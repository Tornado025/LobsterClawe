package com.lobsterclawe.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedRecipeDao {
    @Query("SELECT * FROM saved_recipes ORDER BY savedAt DESC")
    fun getAll(): Flow<List<SavedRecipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(recipe: SavedRecipe)

    @Query("DELETE FROM saved_recipes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM saved_recipes WHERE id = :id")
    fun isSaved(id: String): Flow<Int>
}
