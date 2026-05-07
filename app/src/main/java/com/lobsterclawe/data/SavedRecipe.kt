package com.lobsterclawe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_recipes")
data class SavedRecipe(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val fullJson: String,
    val savedAt: Long = System.currentTimeMillis()
)
