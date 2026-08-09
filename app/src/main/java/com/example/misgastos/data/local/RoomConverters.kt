package com.example.misgastos.data.local

import androidx.room.TypeConverter

class RoomConverters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromThemeMode(mode: ThemeMode): String = mode.name

    @TypeConverter
    fun toThemeMode(value: String): ThemeMode = ThemeMode.valueOf(value)
}
