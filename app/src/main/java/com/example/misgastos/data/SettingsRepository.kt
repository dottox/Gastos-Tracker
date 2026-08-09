package com.example.misgastos.data

import com.example.misgastos.data.local.AppSettings
import com.example.misgastos.data.local.Category
import com.example.misgastos.data.local.DefaultSettings
import com.example.misgastos.data.local.SettingsDao
import com.example.misgastos.data.local.ThemeMode
import com.example.misgastos.data.local.TransactionType
import kotlinx.coroutines.flow.Flow

class SettingsRepository(
    private val dao: SettingsDao
) {
    fun observeSettings(): Flow<AppSettings?> = dao.observeSettings()

    fun observeCategories(type: TransactionType): Flow<List<Category>> = dao.observeCategories(type)

    suspend fun seedDefaults() {
        if (dao.getSettings() != null) return

        val defaultCategories = buildList {
            DefaultSettings.EXPENSE_CATEGORIES.forEach { name ->
                add(
                    Category(
                        name = name,
                        type = TransactionType.GASTO,
                        iconName = DefaultSettings.categoryIcon(TransactionType.GASTO, name)
                    )
                )
            }
            DefaultSettings.INCOME_CATEGORIES.forEach { name ->
                add(
                    Category(
                        name = name,
                        type = TransactionType.INGRESO,
                        iconName = DefaultSettings.categoryIcon(TransactionType.INGRESO, name)
                    )
                )
            }
        }
        dao.seedDefaults(AppSettings(), defaultCategories)
    }

    suspend fun saveSettings(
        decimalPlaces: Int,
        savingsGoalCents: Long,
        themeMode: ThemeMode
    ) {
        dao.upsertSettings(
            AppSettings(
                decimalPlaces = decimalPlaces,
                savingsGoalCents = savingsGoalCents,
                themeMode = themeMode
            )
        )
    }

    suspend fun addCategory(
        type: TransactionType,
        name: String,
        iconName: String = DefaultSettings.DEFAULT_CATEGORY_ICON
    ) {
        val normalizedName = name.trim()
        if (normalizedName.isEmpty() || dao.findCategory(type, normalizedName) != null) return

        dao.insertCategory(
            Category(
                name = normalizedName,
                type = type,
                iconName = iconName.ifBlank { DefaultSettings.DEFAULT_CATEGORY_ICON }
            )
        )
    }

    suspend fun deleteCategory(type: TransactionType, name: String) {
        dao.deleteCategory(type, name)
    }

    suspend fun updateCategoryIcon(type: TransactionType, name: String, iconName: String) {
        dao.updateCategoryIcon(
            type = type,
            name = name,
            iconName = iconName.ifBlank { DefaultSettings.DEFAULT_CATEGORY_ICON }
        )
    }
}
