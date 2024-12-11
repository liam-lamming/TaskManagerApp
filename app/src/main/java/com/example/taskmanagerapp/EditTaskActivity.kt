package com.example.taskmanagerapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class EditTaskActivity : AppCompatActivity() {

    private lateinit var dbHelper: TaskDatabaseHelper
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    companion object {
        private const val TAG = "EditTaskActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        // Initialize views
        val editTextTitle: EditText = findViewById(R.id.editTextTitle)
        val editTextDescription: EditText = findViewById(R.id.editTextDescription)
        val editTextPriority: EditText = findViewById(R.id.editTextPriority)
        val editTextCategory: EditText = findViewById(R.id.editTextCategory)
        val buttonSave: Button = findViewById(R.id.buttonSave)

        // Initialize the database helper
        dbHelper = TaskDatabaseHelper(this)

        // Retrieve the task and position passed from MainActivity
        val task = intent.getParcelableExtra<Task>(MainActivity.EDIT_TASK_EXTRA)
        val position = intent.getIntExtra(MainActivity.TASK_POSITION_EXTRA, -1)

        if (task == null || position == -1) {
            Toast.makeText(this, "Failed to load task details", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Task or position is invalid")
            finish() // Close the activity if task data is null or position is invalid
            return
        }

        // Populate the input fields with the task's current details
        editTextTitle.setText(task.title)
        editTextDescription.setText(task.description)
        editTextPriority.setText(task.priority)
        editTextCategory.setText(task.category)

        // Handle save button click
        buttonSave.setOnClickListener {
            val updatedTitle = editTextTitle.text.toString().trim()
            val updatedDescription = editTextDescription.text.toString().trim()
            val updatedPriority = editTextPriority.text.toString().trim()
            val updatedCategory = editTextCategory.text.toString().trim()

            // Validate inputs
            if (updatedTitle.isEmpty()) {
                editTextTitle.error = "Title is required"
                return@setOnClickListener
            }
            if (updatedDescription.isEmpty()) {
                editTextDescription.error = "Description is required"
                return@setOnClickListener
            }

            // Update the task object
            val updatedTask = task.copy(
                title = updatedTitle,
                description = updatedDescription,
                priority = updatedPriority,
                category = updatedCategory
            )

            // Update the task in SQLite
            val success = dbHelper.upsertTask(updatedTask)
            if (!success) {
                Toast.makeText(this, "Failed to update task in SQLite", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to update task in SQLite for ID: ${updatedTask.id}")
                return@setOnClickListener
            }

            // Update the task in Firebase
            firebaseDatabase.getReference("tasks").child(task.id.toString())
                .setValue(updatedTask)
                .addOnSuccessListener {
                    Log.d(TAG, "Task updated in Firebase with ID: ${task.id}")
                    Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "Failed to update task in Firebase", error)
                    Toast.makeText(this, "Failed to sync task with Firebase", Toast.LENGTH_SHORT).show()
                }

            // Pass the updated task back to MainActivity
            val resultIntent = Intent().apply {
                putExtra(MainActivity.EDIT_TASK_EXTRA, updatedTask)
                putExtra(MainActivity.TASK_POSITION_EXTRA, position)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
