package com.example.taskmanagerapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "task_manager.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_PRIORITY = "priority"
        private const val COLUMN_CATEGORY = "category"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_PRIORITY TEXT,
                $COLUMN_CATEGORY TEXT
            )
        """
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }

    // Add a new task to the database
    fun addTask(task: Task): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_PRIORITY, task.priority)
            put(COLUMN_CATEGORY, task.category)
        }

        return db.insert(TABLE_NAME, null, values)
    }

    // Retrieve all tasks from the database
    fun getAllTasks(): List<Task> {
        val taskList = mutableListOf<Task>()
        val cursor: Cursor = readableDatabase.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_PRIORITY, COLUMN_CATEGORY),
            null, null, null, null, null
        )

        cursor.use {
            it.moveToFirst()
            while (!it.isAfterLast) {
                // Get column indices and check if they are valid
                val idIndex = it.getColumnIndex(COLUMN_ID)
                val titleIndex = it.getColumnIndex(COLUMN_TITLE)
                val descriptionIndex = it.getColumnIndex(COLUMN_DESCRIPTION)
                val priorityIndex = it.getColumnIndex(COLUMN_PRIORITY)
                val categoryIndex = it.getColumnIndex(COLUMN_CATEGORY)

                if (idIndex >= 0 && titleIndex >= 0 && descriptionIndex >= 0 && priorityIndex >= 0 && categoryIndex >= 0) {
                    // Create task if column indices are valid
                    val task = Task(
                        id = it.getInt(idIndex),
                        title = it.getString(titleIndex),
                        description = it.getString(descriptionIndex),
                        priority = it.getString(priorityIndex),
                        category = it.getString(categoryIndex)
                    )
                    taskList.add(task)
                }

                it.moveToNext() // Move to the next row
            }
        }

        cursor.close()
        return taskList
    }

    // Update an existing task
    fun updateTask(task: Task): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_PRIORITY, task.priority)
            put(COLUMN_CATEGORY, task.category)
        }

        return db.update(
            TABLE_NAME,
            values,
            "$COLUMN_ID = ?",
            arrayOf(task.id.toString()) // Update by task ID
        )
    }

    // Delete a task by its ID
    fun deleteTask(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }
}
