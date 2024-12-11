package com.example.taskmanagerapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "task_manager.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_NAME = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_FIREBASE_KEY = "firebase_key"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_PRIORITY = "priority"
        private const val COLUMN_CATEGORY = "category"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_FIREBASE_KEY TEXT UNIQUE,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_PRIORITY TEXT,
                $COLUMN_CATEGORY TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(
                """
                ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_FIREBASE_KEY TEXT DEFAULT ''
                """
            )
        }
    }

    /**
     * Inserts or updates a task in the database.
     */
    fun upsertTask(task: Task): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, task.id)
            put(COLUMN_FIREBASE_KEY, task.firebaseKey)
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_PRIORITY, task.priority)
            put(COLUMN_CATEGORY, task.category)
        }

        val rowsAffected: Int
        val success: Boolean
        if (isTaskInDatabase(task.id)) {
            // Update the task if it already exists
            rowsAffected = db.update(
                TABLE_NAME,
                values,
                "$COLUMN_ID = ?",
                arrayOf(task.id)
            )
            success = rowsAffected > 0
        } else {
            // Insert the task as a new entry
            success = db.insert(TABLE_NAME, null, values) != -1L
        }
        return success
    }

    /**
     * Retrieves all tasks from the database.
     */
    fun getAllTasks(): List<Task> {
        val taskList = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                taskList.add(cursorToTask(it))
            }
        }
        return taskList
    }

    /**
     * Retrieves a task by its ID.
     */
    fun getTaskById(id: String): Task? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id),
            null,
            null,
            null
        )
        var task: Task? = null
        cursor.use {
            if (it.moveToFirst()) {
                task = cursorToTask(it)
            }
        }
        return task
    }

    /**
     * Deletes a task from the database by ID.
     */
    fun deleteTask(taskId: String): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?",
            arrayOf(taskId)
        )
        return rowsDeleted > 0
    }

    /**
     * Checks if a task with the given ID exists in the database.
     */
    private fun isTaskInDatabase(taskId: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID),
            "$COLUMN_ID = ?",
            arrayOf(taskId),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    /**
     * Converts a cursor row into a Task object.
     */
    private fun cursorToTask(cursor: Cursor): Task {
        return Task(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            firebaseKey = cursor.getString(cursor.getColumnIndex(COLUMN_FIREBASE_KEY) ?: -1) ?: "",
            title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
            priority = cursor.getString(cursor.getColumnIndex(COLUMN_PRIORITY) ?: -1) ?: "",
            category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY) ?: -1) ?: ""
        )
    }
}