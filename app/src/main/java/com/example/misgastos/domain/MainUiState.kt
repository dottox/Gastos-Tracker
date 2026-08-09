package com.example.misgastos.domain

import androidx.compose.runtime.Immutable
import com.example.misgastos.data.local.DefaultSettings
import com.example.misgastos.data.local.Transaction
import com.example.misgastos.data.local.TransactionType
import java.time.YearMonth

@Immutable
data class MainUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val transactions: List<Transaction> = emptyList(),
    val incomeTotalCents: Long = 0,
    val expenseTotalCents: Long = 0,
    val historicalSavingsCents: Long = 0,
    val savingsGoalCents: Long = DefaultSettings.SAVINGS_GOAL_CENTS,
    val decimalPlaces: Int = DefaultSettings.DECIMAL_PLACES,
    val categoryIcons: Map<TransactionType, Map<String, String>> = emptyMap()
) {
    val monthlySavingsCents: Long
        get() = incomeTotalCents - expenseTotalCents
}
