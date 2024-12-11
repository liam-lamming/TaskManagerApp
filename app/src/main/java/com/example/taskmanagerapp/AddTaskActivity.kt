package com.example.taskmanagerapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddTaskActivity : AppCompatActivity() {

    private lateinit var dbHelper: TaskDatabaseHelper

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

            // Create a new task object
            val newTask = Task(
                id = 0, // Temporary ID, will be updated after database insertion
                title = title,
                description = description,
                priority = priority,
                category = category
            )

            // Save the task to the SQLite database
            val newTaskId = dbHelper.addTask(newTask)
            if (newTaskId == -1L) {
                Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update the task ID with the value from the database
            newTask.id = newTaskId.toInt()

            // Pass the new task back to MainActivity
            val resultIntent = Intent().apply {
                putExtra(MainActivity.NEW_TASK_EXTRA, newTask)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
