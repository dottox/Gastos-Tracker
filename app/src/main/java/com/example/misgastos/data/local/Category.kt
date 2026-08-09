package com.example.misgastos.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name", "type"], unique = true)]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType,
    @ColumnInfo(name = "icon_name") val iconName: String = DefaultSettings.DEFAULT_CATEGORY_ICON
)
