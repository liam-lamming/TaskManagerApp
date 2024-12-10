package com.example.taskmanagerapp

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskList: MutableList<Task>
    private lateinit var dbHelper: TaskDatabaseHelper

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    companion object {
        const val NEW_TASK_REQUEST_CODE = 1
        const val EDIT_TASK_REQUEST_CODE = 2
        const val NEW_TASK_EXTRA = "NEW_TASK"
        const val EDIT_TASK_EXTRA = "EDIT_TASK"
        const val TASK_POSITION_EXTRA = "TASK_POSITION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = TaskDatabaseHelper(this)

        // Set up Toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set up RecyclerView
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks)
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)

        // Initialize task list and adapter
        taskList = mutableListOf()
        taskAdapter = TaskAdapter(taskList) { task, position ->
            showTaskOptions(task, position)
        }
        recyclerViewTasks.adapter = taskAdapter

        // Set up Floating Action Button
        val fabAddTask: FloatingActionButton = findViewById(R.id.fabAddTask)
        fabAddTask.setOnClickListener { navigateToAddTask() }

        // Load tasks
        loadTasksFromDatabase()
        syncFirebaseToSQLite()
    }

    /**
     * Load tasks from the SQLite database.
     */
    private fun loadTasksFromDatabase() {
        taskList.clear()
        taskList.addAll(dbHelper.getAllTasks())
        taskAdapter.notifyDataSetChanged()
    }

    /**
     * Write a task to Firebase Realtime Database.
     */
    private fun writeToRealtimeDatabase(task: Task) {
        firebaseDatabase.getReference("tasks").child(task.id.toString())
            .setValue(task)
            .addOnSuccessListener {
                Toast.makeText(this, "Task added to Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to write task to Firebase", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Read tasks from Firebase Realtime Database and sync with SQLite.
     */
    private fun syncFirebaseToSQLite() {
        firebaseDatabase.getReference("tasks").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    if (task != null) {
                        dbHelper.addTask(task)
                    }
                }
                loadTasksFromDatabase()
                Toast.makeText(this@MainActivity, "Tasks synced from Firebase", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to sync tasks: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Navigate to AddTaskActivity for creating a new task.
     */
    private fun navigateToAddTask() {
        val intent = Intent(this, AddTaskActivity::class.java)
        startActivityForResult(intent, NEW_TASK_REQUEST_CODE)
    }

    /**
     * Handle task click options: edit or delete.
     */
    private fun showTaskOptions(task: Task, position: Int) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle("Select an option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> navigateToEditTask(task, position)
                    1 -> deleteTask(position)
                }
            }
            .show()
    }

    /**
     * Navigate to EditTaskActivity for editing an existing task.
     */
    private fun navigateToEditTask(task: Task, position: Int) {
        val intent = Intent(this, EditTaskActivity::class.java)
        intent.putExtra(EDIT_TASK_EXTRA, task)
        intent.putExtra(TASK_POSITION_EXTRA, position)
        startActivityForResult(intent, EDIT_TASK_REQUEST_CODE)
    }

    /**
     * Delete a task and refresh the adapter.
     */
    private fun deleteTask(position: Int) {
        val taskToDelete = taskList[position]
        dbHelper.deleteTask(taskToDelete.id)
        taskList.removeAt(position)
        taskAdapter.notifyItemRemoved(position)

        // Remove from Firebase
        firebaseDatabase.getReference("tasks").child(taskToDelete.id.toString()).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Task deleted from Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete task from Firebase", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Handle result from AddTaskActivity and EditTaskActivity.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                NEW_TASK_REQUEST_CODE -> {
                    val newTask = data?.getParcelableExtra<Task>(NEW_TASK_EXTRA)
                    if (newTask != null) {
                        dbHelper.addTask(newTask)
                        writeToRealtimeDatabase(newTask)
                        loadTasksFromDatabase()
                    }
                }
                EDIT_TASK_REQUEST_CODE -> {
                    val updatedTask = data?.getParcelableExtra<Task>(EDIT_TASK_EXTRA)
                    val position = data?.getIntExtra(TASK_POSITION_EXTRA, -1) ?: -1
                    if (updatedTask != null && position >= 0) {
                        dbHelper.updateTask(updatedTask)
                        writeToRealtimeDatabase(updatedTask)
                        taskList[position] = updatedTask
                        taskAdapter.notifyItemChanged(position)
                    }
                }
            }
        }
    }

    /**
     * Inflate the options menu for the toolbar.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    /**
     * Handle toolbar menu item clicks.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
