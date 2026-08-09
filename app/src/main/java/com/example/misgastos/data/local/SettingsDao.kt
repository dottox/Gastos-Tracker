package com.example.misgastos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun observeSettings(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AppSettings?

    @Upsert
    suspend fun upsertSettings(settings: AppSettings)

    @Query(
        "SELECT * FROM categories " +
            "WHERE type = :type ORDER BY name COLLATE NOCASE"
    )
    fun observeCategories(type: TransactionType): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type")
    suspend fun getCategories(type: TransactionType): List<Category>

    @Query("SELECT * FROM categories ORDER BY id")
    suspend fun getAllCategories(): List<Category>

    @Query(
        "SELECT * FROM categories " +
            "WHERE type = :type AND name = :name COLLATE NOCASE LIMIT 1"
    )
    suspend fun findCategory(type: TransactionType, name: String): Category?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<Category>)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    @Transaction
    suspend fun seedDefaults(settings: AppSettings, categories: List<Category>) {
        insertCategories(categories)
        upsertSettings(settings)
    }

    @Query("DELETE FROM categories WHERE type = :type AND name = :name")
    suspend fun deleteCategory(type: TransactionType, name: String)

    @Query("UPDATE categories SET icon_name = :iconName WHERE type = :type AND name = :name")
    suspend fun updateCategoryIcon(type: TransactionType, name: String, iconName: String)
}
