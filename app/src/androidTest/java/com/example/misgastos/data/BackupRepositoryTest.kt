package com.example.misgastos.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.misgastos.data.local.AppDatabase
import com.example.misgastos.data.local.AppSettings
import com.example.misgastos.data.local.Category
import com.example.misgastos.data.local.ThemeMode
import com.example.misgastos.data.local.Transaction
import com.example.misgastos.data.local.TransactionType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: BackupRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = BackupRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun exportAndImportRestoresTransactionsCategoriesAndSettings() = runBlocking {
        val settings = AppSettings(
            decimalPlaces = 1,
            savingsGoalCents = 250_000L,
            themeMode = ThemeMode.OSCURO
        )
        val categories = listOf(
            Category(name = "Casa", type = TransactionType.GASTO, iconName = "Home"),
            Category(name = "Trabajo", type = TransactionType.INGRESO, iconName = "Work")
        )
        database.settingsDao().upsertSettings(settings)
        database.settingsDao().insertCategories(categories)
        database.transactionDao().insert(
            Transaction(
                title = "Alquiler",
                amountCents = 95_000L,
                category = "Casa",
                date = 20_000L,
                type = TransactionType.GASTO
            )
        )
        database.transactionDao().insert(
            Transaction(
                title = "Sueldo",
                amountCents = 260_000L,
                category = "Trabajo",
                date = 20_001L,
                type = TransactionType.INGRESO
            )
        )

        val expectedSettings = database.settingsDao().getSettings()
        val expectedCategories = database.settingsDao().getAllCategories()
        val expectedTransactions = database.transactionDao().getAll()
        val output = ByteArrayOutputStream()

        repository.export(output)

        database.transactionDao().deleteAll()
        database.settingsDao().deleteAllCategories()
        database.settingsDao().upsertSettings(AppSettings())

        repository.import(ByteArrayInputStream(output.toByteArray()))

        assertEquals(expectedSettings, database.settingsDao().getSettings())
        assertEquals(expectedCategories, database.settingsDao().getAllCategories())
        assertEquals(expectedTransactions, database.transactionDao().getAll())
    }

    @Test
    fun invalidBackupDoesNotDeleteExistingData() = runBlocking {
        database.settingsDao().upsertSettings(AppSettings(savingsGoalCents = 300_000L))
        database.settingsDao().insertCategory(
            Category(name = "Comida", type = TransactionType.GASTO, iconName = "Restaurant")
        )
        database.transactionDao().insert(
            Transaction(
                title = "Supermercado",
                amountCents = 18_642L,
                category = "Comida",
                date = 20_000L,
                type = TransactionType.GASTO
            )
        )
        val settingsBefore = database.settingsDao().getSettings()
        val categoriesBefore = database.settingsDao().getAllCategories()
        val transactionsBefore = database.transactionDao().getAll()

        val error = runCatching {
            repository.import(ByteArrayInputStream("{\"format\":\"otro\"}".toByteArray()))
        }.exceptionOrNull()

        assertTrue(error is IOException)
        assertEquals(settingsBefore, database.settingsDao().getSettings())
        assertEquals(categoriesBefore, database.settingsDao().getAllCategories())
        assertEquals(transactionsBefore, database.transactionDao().getAll())
    }
}
