package com.example.misgastos.domain

import androidx.compose.runtime.Immutable
import com.example.misgastos.data.local.Category
import com.example.misgastos.data.local.DefaultSettings
import com.example.misgastos.data.local.ThemeMode
import com.example.misgastos.data.local.TransactionType

private fun defaultCategories(type: TransactionType, names: List<String>): List<Category> =
    names.map { name ->
        Category(
            name = name,
            type = type,
            iconName = DefaultSettings.categoryIcon(type, name)
        )
    }

@Immutable
data class SettingsUiState(
    val decimalPlaces: Int = DefaultSettings.DECIMAL_PLACES,
    val savingsGoalCents: Long = DefaultSettings.SAVINGS_GOAL_CENTS,
    val themeMode: ThemeMode = ThemeMode.SISTEMA,
    val expenseCategories: List<Category> = defaultCategories(
        TransactionType.GASTO,
        DefaultSettings.EXPENSE_CATEGORIES
    ),
    val incomeCategories: List<Category> = defaultCategories(
        TransactionType.INGRESO,
        DefaultSettings.INCOME_CATEGORIES
    )
)
