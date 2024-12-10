package com.example.taskmanagerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditTaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        // Initialize views
        val editTextTitle: EditText = findViewById(R.id.editTextTitle)
        val editTextDescription: EditText = findViewById(R.id.editTextDescription)
        val editTextPriority: EditText = findViewById(R.id.editTextPriority)
        val editTextCategory: EditText = findViewById(R.id.editTextCategory)
        val buttonSave: Button = findViewById(R.id.buttonSave)

        // Retrieve the task passed from MainActivity
        val task = intent.getParcelableExtra<Task>(MainActivity.EDIT_TASK_EXTRA)
        if (task == null) {
            Toast.makeText(this, "Failed to load task details", Toast.LENGTH_SHORT).show()
            finish() // Close the activity if task data is null
            return
        }

        // Populate the input fields with the task's current details
        editTextTitle.setText(task.title)
        editTextDescription.setText(task.description)
        editTextPriority.setText(task.priority)
        editTextCategory.setText(task.category)

        // Handle save button click
        buttonSave.setOnClickListener {
            val updatedTask = task.copy(
                title = editTextTitle.text.toString().trim(),
                description = editTextDescription.text.toString().trim(),
                priority = editTextPriority.text.toString().trim(),
                category = editTextCategory.text.toString().trim()
            )

            // Validate updated fields
            if (updatedTask.title.isEmpty()) {
                editTextTitle.error = "Title is required"
                return@setOnClickListener
            }
            if (updatedTask.description.isEmpty()) {
                editTextDescription.error = "Description is required"
                return@setOnClickListener
            }

            // Pass the updated task back to MainActivity
            val resultIntent = Intent().apply {
                putExtra(MainActivity.EDIT_TASK_EXTRA, updatedTask)
                putExtra(MainActivity.TASK_POSITION_EXTRA, intent.getIntExtra(MainActivity.TASK_POSITION_EXTRA, -1))
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
