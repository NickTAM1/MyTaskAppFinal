package com.vfs.mytaskapp

import com.google.firebase.database.Exclude

data class Task(
    @get:Exclude
    var id: String = "", // Exclude this from Firebase, as it's the key
    var name: String = "",
    var content: String = "",
    var done: Boolean = false,
    var priority: Int = 1 // 0: Low, 1: Medium, 2: High
) {
    // Add a toMap() function for saving data to Firebase
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "content" to content,
            "done" to done,
            "priority" to priority
        )
    }
}