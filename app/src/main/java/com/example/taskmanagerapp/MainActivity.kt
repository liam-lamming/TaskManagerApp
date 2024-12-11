package com.example.taskmanagerapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private lateinit var dbHelper: TaskDatabaseHelper
    private val taskList: MutableList<Task> = mutableListOf()

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private var isSyncing = false

    companion object {
        const val NEW_TASK_EXTRA = "NEW_TASK"
        const val EDIT_TASK_EXTRA = "EDIT_TASK"
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize database and views
        dbHelper = TaskDatabaseHelper(this)
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks)
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)

        // Setup adapter with click listener
        taskAdapter = TaskAdapter { task, position -> showTaskOptions(task, position) }
        recyclerViewTasks.adapter = taskAdapter

        // Floating action button to add a task
        val fabAddTask = findViewById<FloatingActionButton>(R.id.fabAddTask)
        fabAddTask.setOnClickListener { navigateToAddTask() }

        // Load tasks and sync with Firebase
        loadTasksFromDatabase()
        syncFirebaseToSQLite()
    }

    /**
     * Load tasks from SQLite database.
     */
    private fun loadTasksFromDatabase() {
        taskList.clear()
        taskList.addAll(dbHelper.getAllTasks())
        taskAdapter.setTasks(taskList)
    }

    /**
     * Sync tasks from Firebase to SQLite.
     */
    private fun syncFirebaseToSQLite() {
        if (isSyncing) return
        isSyncing = true

        firebaseDatabase.getReference("tasks")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val firebaseTasks = mutableListOf<Task>()

                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)
                        if (task != null) {
                            if (task.firebaseKey.isNotEmpty()) {
                                firebaseTasks.add(task)
                                dbHelper.upsertTask(task) // Use upsert for consistency
                            } else {
                                Log.e(TAG, "Task missing Firebase key: $task")
                            }
                        }
                    }

                    taskList.clear()
                    taskList.addAll(dbHelper.getAllTasks()) // Load from SQLite after sync
                    taskAdapter.setTasks(taskList)
                    Log.d(TAG, "Tasks synced from Firebase")
                    Toast.makeText(this@MainActivity, "Tasks synced successfully", Toast.LENGTH_SHORT).show()

                    isSyncing = false
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Sync cancelled: ${error.message}")
                    Toast.makeText(this@MainActivity, "Sync failed: ${error.message}", Toast.LENGTH_SHORT).show()

                    isSyncing = false
                }
            })
    }

    /**
     * Navigate to AddTaskActivity.
     */
    private fun navigateToAddTask() {
        val intent = Intent(this, AddTaskActivity::class.java)
        startActivityForResult(intent, 1)
    }

    /**
     * Show options (edit/delete) for a task.
     */
    private fun showTaskOptions(task: Task, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Select an option")
            .setItems(arrayOf("Edit", "Delete")) { _, which ->
                when (which) {
                    0 -> navigateToEditTask(task)
                    1 -> deleteTask(task, position)
                }
            }
            .show()
    }

    /**
     * Navigate to EditTaskActivity.
     */
    private fun navigateToEditTask(task: Task) {
        val intent = Intent(this, EditTaskActivity::class.java).apply {
            putExtra(EDIT_TASK_EXTRA, task)
        }
        startActivityForResult(intent, 2)
    }

    /**
     * Delete a task from Firebase and SQLite.
     */
    private fun deleteTask(task: Task, position: Int) {
        dbHelper.deleteTask(task.id)
        taskList.removeAt(position)
        taskAdapter.notifyItemRemoved(position)

        firebaseDatabase.getReference("tasks").child(task.firebaseKey).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show()
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
                1 -> { // Adding new task
                    val newTask = data?.getParcelableExtra<Task>(NEW_TASK_EXTRA)
                    newTask?.let {
                        dbHelper.upsertTask(it) // Upsert directly
                        taskList.add(it)
                        taskAdapter.notifyItemInserted(taskList.size - 1)
                    }
                }
                2 -> { // Editing existing task
                    val updatedTask = data?.getParcelableExtra<Task>(EDIT_TASK_EXTRA)
                    updatedTask?.let {
                        dbHelper.upsertTask(it) // Upsert directly
                        val index = taskList.indexOfFirst { task -> task.id == it.id }
                        if (index != -1) {
                            taskList[index] = it
                            taskAdapter.notifyItemChanged(index)
                        }
                    }
                }
            }
        }
    }
}
