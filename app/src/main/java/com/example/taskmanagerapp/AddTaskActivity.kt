package com.example.taskmanagerapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class AddTaskActivity : AppCompatActivity() {

    private lateinit var dbHelper: TaskDatabaseHelper
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    companion object {
        private const val TAG = "AddTaskActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        // Initialize views
        val editTextTitle: EditText = findViewById(R.id.editTextTitle)
        val editTextDescription: EditText = findViewById(R.id.editTextDescription)
        val editTextPriority: EditText = findViewById(R.id.editTextPriority)
        val editTextCategory: EditText = findViewById(R.id.editTextCategory)
        val buttonSave: Button = findViewById(R.id.buttonSave)

        // Initialize the database helper
        dbHelper = TaskDatabaseHelper(this)

        // Handle save button click
        buttonSave.setOnClickListener {
            val title = editTextTitle.text.toString().trim()
            val description = editTextDescription.text.toString().trim()
            val priority = editTextPriority.text.toString().trim()
            val category = editTextCategory.text.toString().trim()

            // Validate inputs
            if (title.isEmpty()) {
                editTextTitle.error = "Title is required"
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                editTextDescription.error = "Description is required"
                return@setOnClickListener
            }

            // Generate a unique task ID using Firebase
            val taskId = firebaseDatabase.getReference("tasks").push().key
            if (taskId == null) {
                Toast.makeText(this, "Failed to generate task ID", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to generate unique task ID from Firebase")
                return@setOnClickListener
            }

            // Create a new task object with the generated ID
            val newTask = Task(
                id = taskId.hashCode(), // Use the hash of the Firebase ID as the SQLite ID
                title = title,
                description = description,
                priority = priority,
                category = category
            )

            // Save the task to SQLite
            val success = dbHelper.upsertTask(newTask)
            if (!success) {
                Toast.makeText(this, "Failed to save task in SQLite", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to save task in SQLite")
                return@setOnClickListener
            }

            // Save the task to Firebase
            firebaseDatabase.getReference("tasks").child(taskId)
                .setValue(newTask)
                .addOnSuccessListener {
                    Log.d(TAG, "Task saved to Firebase with ID: $taskId")
                    Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "Failed to save task to Firebase", error)
                    Toast.makeText(this, "Failed to sync with Firebase", Toast.LENGTH_SHORT).show()
                }

            // Pass the new task back to MainActivity
            val resultIntent = Intent().apply {
                putExtra(MainActivity.NEW_TASK_EXTRA, newTask)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
