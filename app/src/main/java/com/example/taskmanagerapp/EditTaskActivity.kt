package com.example.taskmanagerapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
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
        val editTextTitle: EditText = findViewById(R.id.editTitle)
        val editTextDescription: EditText = findViewById(R.id.editDescription)
        val spinnerPriority: Spinner = findViewById(R.id.editPriority)
        val spinnerCategory: Spinner = findViewById(R.id.editCategory)
        val buttonSave: Button = findViewById(R.id.btnSaveTask)

        // Initialize the database helper
        dbHelper = TaskDatabaseHelper(this)

        // Retrieve the task passed from MainActivity
        val task = intent.getParcelableExtra<Task>(MainActivity.EDIT_TASK_EXTRA)
        if (task == null) {
            Toast.makeText(this, "Failed to load task details", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Task is invalid")
            finish()
            return
        }

        // Populate input fields with task's current details
        editTextTitle.setText(task.title)
        editTextDescription.setText(task.description)
        spinnerPriority.setSelection(getSpinnerIndex(spinnerPriority, task.priority))
        spinnerCategory.setSelection(getSpinnerIndex(spinnerCategory, task.category))

        // Handle save button click
        buttonSave.setOnClickListener {
            val updatedTitle = editTextTitle.text.toString().trim()
            val updatedDescription = editTextDescription.text.toString().trim()
            val updatedPriority = spinnerPriority.selectedItem?.toString() ?: ""
            val updatedCategory = spinnerCategory.selectedItem?.toString() ?: ""

            // Validate inputs
            if (updatedTitle.isEmpty()) {
                editTextTitle.error = "Title is required"
                return@setOnClickListener
            }
            if (updatedDescription.isEmpty()) {
                editTextDescription.error = "Description is required"
                return@setOnClickListener
            }

            // Update task object
            val updatedTask = task.copy(
                title = updatedTitle,
                description = updatedDescription,
                priority = updatedPriority,
                category = updatedCategory
            )

            // Update SQLite
            if (!dbHelper.upsertTask(updatedTask)) {
                Toast.makeText(this, "Failed to update task in SQLite", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to update task in SQLite for ID: ${updatedTask.id}")
                return@setOnClickListener
            }

            // Update Firebase
            firebaseDatabase.getReference("tasks").child(task.firebaseKey)
                .setValue(updatedTask)
                .addOnSuccessListener {
                    Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Failed to sync task with Firebase", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Failed to update task in Firebase", error)
                }

            // Return to MainActivity
            val resultIntent = Intent().apply {
                putExtra(MainActivity.EDIT_TASK_EXTRA, updatedTask)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun getSpinnerIndex(spinner: Spinner, value: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) {
                return i
            }
        }
        return 0
    }
}
