package com.example.taskmanagerapp

import java.io.Serializable

data class Task(
    val id: Int = 0, // Integer value for id instead of String
    val title: String,
    val description: String,
    val priority: String,
    val category: String
) : Serializable
