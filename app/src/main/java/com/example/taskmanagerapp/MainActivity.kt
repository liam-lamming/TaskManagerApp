package com.example.taskmanagerapp

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private lateinit var taskList: ArrayList<Task>
    private lateinit var dbHelper: TaskDatabaseHelper

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private var isSyncing = false

    companion object {
        const val NEW_TASK_EXTRA = "NEW_TASK"
        const val EDIT_TASK_EXTRA = "EDIT_TASK"
        const val TASK_POSITION_EXTRA = "TASK_POSITION"
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = TaskDatabaseHelper(this)
        taskList = ArrayList()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks)
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)

        taskAdapter = TaskAdapter { task, position -> showTaskOptions(task, position) }
        recyclerViewTasks.adapter = taskAdapter

        val fabAddTask = findViewById<FloatingActionButton>(R.id.fabAddTask)
        fabAddTask.setOnClickListener { navigateToAddTask() }

        loadTasksFromDatabase()
        syncFirebaseToSQLite()
    }

    private fun loadTasksFromDatabase() {
        taskList.clear()
        taskList.addAll(dbHelper.getAllTasks())
        taskAdapter.setTasks(taskList)
    }

    private fun writeToRealtimeDatabase(task: Task) {
        Log.d(TAG, "Writing task to Firebase: ${task.title}")
        firebaseDatabase.getReference("tasks").child(task.id.toString())
            .setValue(task)
            .addOnSuccessListener {
                Toast.makeText(this, "Task added to Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to write task to Firebase", error)
                Toast.makeText(this, "Failed to write task to Firebase", Toast.LENGTH_SHORT).show()
            }
    }

    private fun syncFirebaseToSQLite() {
        if (isSyncing) return
        isSyncing = true

        firebaseDatabase.getReference("tasks")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)
                        if (task != null && !isTaskInDatabase(task.id)) {
                            dbHelper.addTask(task)
                        }
                    }
                    loadTasksFromDatabase()
                    Toast.makeText(this@MainActivity, "Tasks synced from Firebase", Toast.LENGTH_SHORT).show()
                    isSyncing = false
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Firebase sync failed", error.toException())
                    Toast.makeText(this@MainActivity, "Sync failed: ${error.message}", Toast.LENGTH_SHORT).show()
                    isSyncing = false
                }
            })
    }

    private fun isTaskInDatabase(taskId: Int): Boolean {
        return dbHelper.getTaskById(taskId) != null
    }

    private fun navigateToAddTask() {
        val intent = Intent(this, AddTaskActivity::class.java)
        startActivityForResult(intent, 1)
    }

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

    private fun navigateToEditTask(task: Task, position: Int) {
        val intent = Intent(this, EditTaskActivity::class.java)
        intent.putExtra(EDIT_TASK_EXTRA, task)
        intent.putExtra(TASK_POSITION_EXTRA, position)
        startActivityForResult(intent, 2)
    }

    private fun deleteTask(position: Int) {
        val taskToDelete = taskList[position]
        dbHelper.deleteTask(taskToDelete.id)
        taskAdapter.removeTask(position)

        firebaseDatabase.getReference("tasks").child(taskToDelete.id.toString()).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Task deleted from Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to delete task from Firebase", error)
                Toast.makeText(this, "Failed to delete task from Firebase", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1 -> { // New Task
                    val newTask = data?.getParcelableExtra<Task>(NEW_TASK_EXTRA)
                    if (newTask != null) {
                        dbHelper.addTask(newTask)
                        writeToRealtimeDatabase(newTask)
                        loadTasksFromDatabase()
                    }
                }
                2 -> { // Edit Task
                    val updatedTask = data?.getParcelableExtra<Task>(EDIT_TASK_EXTRA)
                    val position = data?.getIntExtra(TASK_POSITION_EXTRA, -1) ?: -1
                    if (updatedTask != null && position >= 0) {
                        dbHelper.updateTask(updatedTask)
                        writeToRealtimeDatabase(updatedTask)
                        loadTasksFromDatabase()
                    }
                }
            }
        }
    }
}
