package com.example.misgastos.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT * FROM transactions
        WHERE date_epoch_day >= :startDate AND date_epoch_day < :endDate
        ORDER BY date_epoch_day DESC, id DESC
        """
    )
    fun observeTransactionsForMonth(startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query(
        """
        SELECT COALESCE(SUM(amount_cents), 0) FROM transactions
        WHERE type = :type
          AND date_epoch_day >= :startDate AND date_epoch_day < :endDate
        """
    )
    fun observeMonthlyTotal(
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): Flow<Long>

    @Query(
        """
        SELECT
            COALESCE(
                SUM(
                    CASE
                        WHEN type = 'INGRESO'
                            AND date_epoch_day >= :startDate
                            AND date_epoch_day < :endDate
                        THEN amount_cents
                        ELSE 0
                    END
                ),
                0
            ) AS incomeCents,
            COALESCE(
                SUM(
                    CASE
                        WHEN type = 'GASTO'
                            AND date_epoch_day >= :startDate
                            AND date_epoch_day < :endDate
                        THEN amount_cents
                        ELSE 0
                    END
                ),
                0
            ) AS expenseCents,
            COALESCE(
                SUM(CASE WHEN type = 'INGRESO' THEN amount_cents ELSE -amount_cents END),
                0
            ) AS historicalSavingsCents
        FROM transactions
        """
    )
    fun observeMonthlySummary(startDate: Long, endDate: Long): Flow<MonthlySummary>

    @Query(
        """
        SELECT COALESCE(
            SUM(CASE WHEN type = 'INGRESO' THEN amount_cents ELSE -amount_cents END),
            0
        ) FROM transactions
        """
    )
    fun observeHistoricalSavings(): Flow<Long>

    @Query("SELECT * FROM transactions ORDER BY id")
    suspend fun getAll(): List<Transaction>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)
}
