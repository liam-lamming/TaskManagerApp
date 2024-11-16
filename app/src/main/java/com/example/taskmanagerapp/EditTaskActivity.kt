package com.example.taskmanagerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class EditTaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        val editTextTitle: EditText = findViewById(R.id.editTextTitle)
        val editTextDescription: EditText = findViewById(R.id.editTextDescription)
        val editTextPriority: EditText = findViewById(R.id.editTextPriority)
        val editTextCategory: EditText = findViewById(R.id.editTextCategory)
        val buttonSave: Button = findViewById(R.id.buttonSave)

        // Get the task passed from MainActivity
        val task = intent.getSerializableExtra("EDIT_TASK") as Task
        editTextTitle.setText(task.title)
        editTextDescription.setText(task.description)
        editTextPriority.setText(task.priority)
        editTextCategory.setText(task.category)

        buttonSave.setOnClickListener {
            val updatedTask = Task(
                id = task.id, // Ensure the ID is passed unchanged
                title = editTextTitle.text.toString(),
                description = editTextDescription.text.toString(),
                priority = editTextPriority.text.toString(),
                category = editTextCategory.text.toString()
            )
            val resultIntent = Intent()
            resultIntent.putExtra("UPDATED_TASK", updatedTask)
            resultIntent.putExtra("TASK_POSITION", intent.getIntExtra("TASK_POSITION", -1))
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
