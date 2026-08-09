package com.example.misgastos.data

import com.example.misgastos.data.local.Transaction
import com.example.misgastos.data.local.TransactionDao
import com.example.misgastos.data.local.TransactionType
import com.example.misgastos.data.local.MonthlySummary
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val dao: TransactionDao
) {
    fun observeTransactionsForMonth(month: YearMonth): Flow<List<Transaction>> =
        dao.observeTransactionsForMonth(
            startDate = month.atDay(1).toEpochDay(),
            endDate = month.plusMonths(1).atDay(1).toEpochDay()
        )

    fun observeMonthlyTotal(month: YearMonth, type: TransactionType): Flow<Long> =
        dao.observeMonthlyTotal(
            type = type,
            startDate = month.atDay(1).toEpochDay(),
            endDate = month.plusMonths(1).atDay(1).toEpochDay()
        )

    fun observeMonthlySummary(month: YearMonth): Flow<MonthlySummary> =
        dao.observeMonthlySummary(
            startDate = month.atDay(1).toEpochDay(),
            endDate = month.plusMonths(1).atDay(1).toEpochDay()
        )

    fun observeHistoricalSavings(): Flow<Long> = dao.observeHistoricalSavings()

    suspend fun insert(transaction: Transaction) {
        dao.insert(transaction)
    }

    suspend fun update(transaction: Transaction) {
        dao.update(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        dao.delete(transaction)
    }
}
