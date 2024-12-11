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
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_PRIORITY TEXT,
                $COLUMN_CATEGORY TEXT
            )
        """.trimIndent()
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }

    /**
     * Add a new task to the database.
     */
    fun addTask(task: Task): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_PRIORITY, task.priority)
            put(COLUMN_CATEGORY, task.category)
        }

        return try {
            val taskId = db.insertOrThrow(TABLE_NAME, null, values)
            taskId
        } catch (e: Exception) {
            -1L // Return -1 to indicate failure
        } finally {
            db.close()
        }
    }

    /**
     * Retrieve all tasks from the database.
     */
    fun getAllTasks(): List<Task> {
        val taskList = mutableListOf<Task>()
        val query = "SELECT * FROM $TABLE_NAME"
        val db = readableDatabase
        val cursor: Cursor? = db.rawQuery(query, null)

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val task = Task(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                        title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                        description = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        priority = it.getString(it.getColumnIndexOrThrow(COLUMN_PRIORITY)),
                        category = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY))
                    )
                    taskList.add(task)
                } while (it.moveToNext())
            }
        }

        db.close()
        return taskList
    }

    /**
     * Update an existing task.
     */
    fun updateTask(task: Task): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_PRIORITY, task.priority)
            put(COLUMN_CATEGORY, task.category)
        }

        return try {
            db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(task.id.toString()))
        } catch (e: Exception) {
            0 // Return 0 to indicate failure
        } finally {
            db.close()
        }
    }

    /**
     * Delete a task by its ID.
     */
    fun deleteTask(id: Int): Int {
        val db = writableDatabase
        return try {
            db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
        } catch (e: Exception) {
            0 // Return 0 to indicate failure
        } finally {
            db.close()
        }
    }

    /**
     * Clear all tasks from the database (for testing or reset purposes).
     */
    fun clearTasks(): Int {
        val db = writableDatabase
        return try {
            db.delete(TABLE_NAME, null, null)
        } catch (e: Exception) {
            0 // Return 0 to indicate failure
        } finally {
            db.close()
        }
    }

    /**
     * Retrieve a task by its ID.
     */
    fun getTaskById(id: Int): Task? {
        val db = readableDatabase
        val cursor: Cursor? = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        var task: Task? = null
        cursor?.use {
            if (it.moveToFirst()) {
                task = Task(
                    id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                    title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                    description = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    priority = it.getString(it.getColumnIndexOrThrow(COLUMN_PRIORITY)),
                    category = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY))
                )
            }
        }

        db.close()
        return task
    }
}
