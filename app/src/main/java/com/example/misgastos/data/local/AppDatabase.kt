package com.example.misgastos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Transaction::class, AppSettings::class, Category::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mis_gastos.db"
                )
                    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS app_settings (
                        id INTEGER NOT NULL PRIMARY KEY,
                        decimal_places INTEGER NOT NULL,
                        savings_goal_cents INTEGER NOT NULL,
                        theme_mode TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_categories_name_type
                    ON categories (name, type)
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE categories ADD COLUMN icon_name TEXT NOT NULL DEFAULT 'Category'"
                )
                db.execSQL("UPDATE categories SET icon_name = 'Home' WHERE name = 'Casa' AND type = 'GASTO'")
                db.execSQL("UPDATE categories SET icon_name = 'Restaurant' WHERE name = 'Comida' AND type = 'GASTO'")
                db.execSQL("UPDATE categories SET icon_name = 'Person' WHERE name = 'Personal' AND type = 'GASTO'")
                db.execSQL("UPDATE categories SET icon_name = 'Receipt' WHERE name = 'Servicios' AND type = 'GASTO'")
                db.execSQL("UPDATE categories SET icon_name = 'Payments' WHERE name = 'Sueldo' AND type = 'INGRESO'")
                db.execSQL("UPDATE categories SET icon_name = 'Help' WHERE name = 'Ayuda' AND type = 'INGRESO'")
                db.execSQL("UPDATE categories SET icon_name = 'CreditCard' WHERE name = 'Deuda' AND type = 'INGRESO'")
                db.execSQL("UPDATE categories SET icon_name = 'MoreHoriz' WHERE name = 'Otros'")
            }
        }
    }
}
