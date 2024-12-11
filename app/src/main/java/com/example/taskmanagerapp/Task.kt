package com.example.taskmanagerapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    var id: Int = 0,
    var title: String = "",
    var description: String = "",
    var priority: String = "",
    var category: String = ""
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Task) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
