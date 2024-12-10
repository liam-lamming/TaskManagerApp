package com.example.taskmanagerapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    var id: Int = 0,              // Default values ensure compatibility
    var title: String = "",
    var description: String = "",
    var priority: String = "",
    var category: String = ""
) : Parcelable
