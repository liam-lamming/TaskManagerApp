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
        const val TASK_POSITION_EXTRA = "TASK_POSITION"
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
                            firebaseTasks.add(task)
                            dbHelper.upsertTask(task) // Use upsert for consistency
                        }
                    }

                    taskList.clear()
                    taskList.addAll(firebaseTasks)
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
     * Write a task to Firebase Realtime Database.
     */
    private fun writeToRealtimeDatabase(task: Task) {
        val taskRef = firebaseDatabase.getReference("tasks").child(task.id.toString())

        taskRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) { // Only add if it doesn't already exist
                taskRef.setValue(task)
                    .addOnSuccessListener {
                        Log.d(TAG, "Task added to Firebase: ${task.title}")
                        Toast.makeText(this, "Task added to Firebase", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { error ->
                        Log.e(TAG, "Failed to write task to Firebase", error)
                        Toast.makeText(this, "Failed to sync with Firebase", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Log.d(TAG, "Task already exists in Firebase: ${task.title}")
            }
        }
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
                    0 -> navigateToEditTask(task, position)
                    1 -> deleteTask(task, position)
                }
            }
            .show()
    }

    /**
     * Navigate to EditTaskActivity.
     */
    private fun navigateToEditTask(task: Task, position: Int) {
        val intent = Intent(this, EditTaskActivity::class.java).apply {
            putExtra(EDIT_TASK_EXTRA, task)
            putExtra(TASK_POSITION_EXTRA, position)
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

        firebaseDatabase.getReference("tasks").child(task.id.toString()).removeValue()
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
                        writeToRealtimeDatabase(it)
                        loadTasksFromDatabase()
                    }
                }
                2 -> { // Editing existing task
                    val updatedTask = data?.getParcelableExtra<Task>(EDIT_TASK_EXTRA)
                    updatedTask?.let {
                        dbHelper.upsertTask(it) // Upsert directly
                        writeToRealtimeDatabase(it)
                        loadTasksFromDatabase()
                    }
                }
            }
        }
    }
}
