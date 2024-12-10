package com.example.taskmanagerapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddTaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val editTextTitle: EditText = findViewById(R.id.editTextTitle)
        val editTextDescription: EditText = findViewById(R.id.editTextDescription)
        val editTextPriority: EditText = findViewById(R.id.editTextPriority)
        val editTextCategory: EditText = findViewById(R.id.editTextCategory)
        val buttonSave: Button = findViewById(R.id.buttonSave)

        buttonSave.setOnClickListener { view: View? ->
            val title = editTextTitle.text.toString().trim()
            val description = editTextDescription.text.toString().trim()
            val priority = editTextPriority.text.toString().trim()
            val category = editTextCategory.text.toString().trim()

            if (title.isNotEmpty() && description.isNotEmpty()) {
                val newTask = Task(
                    title = title,
                    description = description,
                    priority = priority,
                    category = category
                )
                val resultIntent = Intent() // Explicitly create a new Intent
                resultIntent.putExtra(MainActivity.NEW_TASK_EXTRA, newTask)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                if (title.isEmpty()) editTextTitle.error = "Title is required"
                if (description.isEmpty()) editTextDescription.error = "Description is required"
            }
        }
    }
}
