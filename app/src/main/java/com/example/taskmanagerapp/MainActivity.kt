package com.example.taskmanagerapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskList: MutableList<Task>
    private lateinit var dbHelper: TaskDatabaseHelper

    companion object {
        const val NEW_TASK_REQUEST_CODE = 1
        const val EDIT_TASK_REQUEST_CODE = 2
        const val NEW_TASK_EXTRA = "NEW_TASK"
        const val EDIT_TASK_EXTRA = "EDIT_TASK"
        const val TASK_POSITION_EXTRA = "TASK_POSITION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = TaskDatabaseHelper(this)

        // Set up Toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set up RecyclerView
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks)
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)

        // Initialize task list and adapter
        taskList = dbHelper.getAllTasks().toMutableList()
        taskAdapter = TaskAdapter(taskList) { task, position ->
            showTaskOptions(task, position)
        }
        recyclerViewTasks.adapter = taskAdapter

        // Set up Floating Action Button
        val fabAddTask: FloatingActionButton = findViewById(R.id.fabAddTask)
        fabAddTask.setOnClickListener {
            navigateToAddTask()
        }

        // Load data from database
        loadTasksFromDatabase()
    }

    /**
     * Load tasks from the SQLite database.
     */
    private fun loadTasksFromDatabase() {
        taskList.clear()
        taskList.addAll(dbHelper.getAllTasks())
        taskAdapter.notifyDataSetChanged()
    }

    /**
     * Navigate to AddTaskActivity for creating a new task.
     */
    private fun navigateToAddTask() {
        val intent = Intent(this, AddTaskActivity::class.java)
        startActivityForResult(intent, NEW_TASK_REQUEST_CODE)
    }

    /**
     * Handle task click options: edit or delete.
     */
    private fun showTaskOptions(task: Task, position: Int) {
        // Example dialog options (you can use AlertDialog or a BottomSheetDialog)
        val options = arrayOf("Edit", "Delete")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select an option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> navigateToEditTask(task, position)
                    1 -> deleteTask(position)
                }
            }
            .show()
    }

    /**
     * Navigate to EditTaskActivity for editing an existing task.
     */
    private fun navigateToEditTask(task: Task, position: Int) {
        val intent = Intent(this, EditTaskActivity::class.java)
        intent.putExtra(EDIT_TASK_EXTRA, task)
        intent.putExtra(TASK_POSITION_EXTRA, position)
        startActivityForResult(intent, EDIT_TASK_REQUEST_CODE)
    }

    /**
     * Delete or Update a task and refresh the adapter.
     */
    // Delete task method
    private fun deleteTask(position: Int) {
        val taskToDelete = taskList[position]
        dbHelper.deleteTask(taskToDelete.id)
        taskList.removeAt(position)
        taskAdapter.notifyItemRemoved(position)
        Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
    }

    // Update task method
    private fun updateTask(position: Int, updatedTask: Task) {
        taskList[position] = updatedTask
        taskAdapter.notifyItemChanged(position)
        Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show()
    }

    /**
     * Handle result from AddTaskActivity and EditTaskActivity.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                NEW_TASK_REQUEST_CODE -> {
                    val newTask = data?.getSerializableExtra(NEW_TASK_EXTRA) as? Task
                    newTask?.let {
                        dbHelper.addTask(it)
                        loadTasksFromDatabase()
                    }
                }
                EDIT_TASK_REQUEST_CODE -> {
                    val updatedTask = data?.getSerializableExtra(EDIT_TASK_EXTRA) as? Task
                    val position = data?.getIntExtra(TASK_POSITION_EXTRA, -1) ?: -1
                    if (updatedTask != null && position >= 0) {
                        dbHelper.updateTask(updatedTask)
                        loadTasksFromDatabase()
                        taskAdapter.notifyItemChanged(position)
                    }
                }
            }
        }
    }

    /**
     * Inflate the options menu for the toolbar.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    /**
     * Handle toolbar menu item clicks.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
