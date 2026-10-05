package com.rizowan.taskr.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a category or tag for a task.
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int, // Color hex or resource id
    val isSystem: Boolean = false // e.g. default categories that cannot be deleted
)
