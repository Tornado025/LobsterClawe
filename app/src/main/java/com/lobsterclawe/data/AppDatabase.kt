package com.lobsterclawe.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SavedRecipe::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedRecipeDao(): SavedRecipeDao

    companion object {
        fun build(context: Context) = Room.databaseBuilder(
            context, AppDatabase::class.java, "lobster.db"
        ).build()
    }
}
