package com.example.taskmanagerapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import java.util.*

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
        val editTextTitle: EditText = findViewById(R.id.inputTitle)
        val editTextDescription: EditText = findViewById(R.id.inputDescription)
        val spinnerPriority: Spinner = findViewById(R.id.inputPriority)
        val spinnerCategory: Spinner = findViewById(R.id.inputCategory)
        val buttonSave: Button = findViewById(R.id.btnSaveTask)

        // Initialize the database helper
        dbHelper = TaskDatabaseHelper(this)

        // Handle save button click
        buttonSave.setOnClickListener {
            val title = editTextTitle.text.toString().trim()
            val description = editTextDescription.text.toString().trim()
            val priority = spinnerPriority.selectedItem?.toString() ?: ""
            val category = spinnerCategory.selectedItem?.toString() ?: ""

            // Validate inputs
            if (title.isEmpty()) {
                editTextTitle.error = "Title is required"
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                editTextDescription.error = "Description is required"
                return@setOnClickListener
            }

            // Generate a unique task ID
            val firebaseKey = firebaseDatabase.getReference("tasks").push().key
            if (firebaseKey == null) {
                Toast.makeText(this, "Failed to generate task ID", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to generate unique task ID from Firebase")
                return@setOnClickListener
            }

            val taskId = UUID.randomUUID().toString()

            // Create a new task object
            val newTask = Task(
                id = taskId,
                firebaseKey = firebaseKey,
                title = title,
                description = description,
                priority = priority,
                category = category
            )

            // Save to SQLite
            if (!dbHelper.upsertTask(newTask)) {
                Toast.makeText(this, "Failed to save task in SQLite", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to save task in SQLite")
                return@setOnClickListener
            }

            // Save to Firebase
            firebaseDatabase.getReference("tasks").child(firebaseKey)
                .setValue(newTask)
                .addOnSuccessListener {
                    Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Failed to sync with Firebase", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Failed to save task to Firebase", error)
                }

            // Return to MainActivity
            val resultIntent = Intent().apply {
                putExtra(MainActivity.NEW_TASK_EXTRA, newTask)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
