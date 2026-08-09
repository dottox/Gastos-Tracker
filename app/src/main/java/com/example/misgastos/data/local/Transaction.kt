package com.example.misgastos.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.compose.runtime.Immutable

enum class TransactionType {
    INGRESO,
    GASTO
}

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["date_epoch_day"])]
)
@Immutable
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "amount_cents")
    val amountCents: Long,
    val category: String,
    @ColumnInfo(name = "date_epoch_day")
    val date: Long,
    val type: TransactionType
)
