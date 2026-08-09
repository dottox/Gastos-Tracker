package com.example.misgastos.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ThemeMode {
    SISTEMA,
    CLARO,
    OSCURO
}

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "decimal_places") val decimalPlaces: Int = DefaultSettings.DECIMAL_PLACES,
    @ColumnInfo(name = "savings_goal_cents") val savingsGoalCents: Long = DefaultSettings.SAVINGS_GOAL_CENTS,
    @ColumnInfo(name = "theme_mode") val themeMode: ThemeMode = ThemeMode.SISTEMA
)

object DefaultSettings {
    const val DECIMAL_PLACES = 2
    const val SAVINGS_GOAL_CENTS = 100_000L
    const val DEFAULT_CATEGORY_ICON = "Category"

    val EXPENSE_CATEGORIES = listOf("Casa", "Comida", "Personal", "Servicios", "Otros")
    val INCOME_CATEGORIES = listOf("Sueldo", "Ayuda", "Deuda", "Otros")

    fun categoryIcon(type: TransactionType, name: String): String = when (type) {
        TransactionType.GASTO -> when (name) {
            "Casa" -> "Home"
            "Comida" -> "Restaurant"
            "Personal" -> "Person"
            "Servicios" -> "Receipt"
            else -> "MoreHoriz"
        }

        TransactionType.INGRESO -> when (name) {
            "Sueldo" -> "Payments"
            "Ayuda" -> "Help"
            "Deuda" -> "CreditCard"
            else -> "MoreHoriz"
        }
    }
}
