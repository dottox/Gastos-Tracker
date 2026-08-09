package com.example.misgastos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.misgastos.data.local.AppDatabase
import com.example.misgastos.data.local.AppSettings
import com.example.misgastos.data.local.Category
import com.example.misgastos.data.local.DefaultSettings
import com.example.misgastos.data.local.Transaction
import com.example.misgastos.data.local.TransactionType
import com.example.misgastos.data.local.ThemeMode
import java.time.YearMonth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun monthlyAndHistoricalTotalsUseTheTransactionType() = runBlocking {
        val dao = database.transactionDao()
        val month = YearMonth.of(2026, 5)
        val income = Transaction(
            title = "Sueldo",
            amountCents = 100_000,
            category = "Trabajo",
            date = month.atDay(5).toEpochDay(),
            type = TransactionType.INGRESO
        )
        val expense = Transaction(
            title = "Alquiler",
            amountCents = 25_000,
            category = "Hogar",
            date = month.atDay(10).toEpochDay(),
            type = TransactionType.GASTO
        )
        val previousIncome = income.copy(
            title = "Ingreso anterior",
            date = month.minusMonths(1).atDay(28).toEpochDay()
        )

        dao.insert(income)
        dao.insert(expense)
        dao.insert(previousIncome)

        assertEquals(2, dao.observeTransactionsForMonth(
            month.atDay(1).toEpochDay(),
            month.plusMonths(1).atDay(1).toEpochDay()
        ).first().size)
        assertEquals(
            100_000L,
            dao.observeMonthlyTotal(
                TransactionType.INGRESO,
                month.atDay(1).toEpochDay(),
                month.plusMonths(1).atDay(1).toEpochDay()
            ).first()
        )
        assertEquals(
            25_000L,
            dao.observeMonthlyTotal(
                TransactionType.GASTO,
                month.atDay(1).toEpochDay(),
                month.plusMonths(1).atDay(1).toEpochDay()
            ).first()
        )
        assertEquals(125_000L, dao.observeHistoricalSavings().first())
        val summary = dao.observeMonthlySummary(
            month.atDay(1).toEpochDay(),
            month.plusMonths(1).atDay(1).toEpochDay()
        ).first()
        assertEquals(100_000L, summary.incomeCents)
        assertEquals(25_000L, summary.expenseCents)
        assertEquals(125_000L, summary.historicalSavingsCents)
    }

    @Test
    fun transactionCanBeUpdatedAndDeleted() = runBlocking {
        val dao = database.transactionDao()
        val transaction = Transaction(
            title = "Cafe",
            amountCents = 500,
            category = "Comida",
            date = YearMonth.of(2026, 5).atDay(1).toEpochDay(),
            type = TransactionType.GASTO
        )
        val id = dao.insert(transaction)
        val saved = transaction.copy(id = id)
        dao.update(saved.copy(title = "Cafe y tostada", amountCents = 750))

        assertEquals("Cafe y tostada", dao.observeTransactionsForMonth(
            YearMonth.of(2026, 5).atDay(1).toEpochDay(),
            YearMonth.of(2026, 6).atDay(1).toEpochDay()
        ).first().single().title)

        dao.delete(saved.copy(title = "Cafe y tostada", amountCents = 750))
        assertEquals(0, dao.observeTransactionsForMonth(
            YearMonth.of(2026, 5).atDay(1).toEpochDay(),
            YearMonth.of(2026, 6).atDay(1).toEpochDay()
        ).first().size)
    }

    @Test
    fun settingsAndCategoriesRemainSeparateByTransactionType() = runBlocking {
        val settingsDao = database.settingsDao()
        settingsDao.upsertSettings(
            AppSettings(
                decimalPlaces = 1,
                savingsGoalCents = 250_000L,
                themeMode = ThemeMode.OSCURO
            )
        )
        settingsDao.insertCategory(Category(name = "Trabajo", type = TransactionType.INGRESO))
        settingsDao.insertCategory(Category(name = "Trabajo", type = TransactionType.GASTO))

        val settings = settingsDao.observeSettings().first()
        assertEquals(1, settings?.decimalPlaces)
        assertEquals(250_000L, settings?.savingsGoalCents)
        assertEquals(ThemeMode.OSCURO, settings?.themeMode)
        assertEquals(
            listOf("Trabajo"),
            settingsDao.observeCategories(TransactionType.INGRESO).first().map { it.name }
        )
        assertEquals(
            listOf("Trabajo"),
            settingsDao.observeCategories(TransactionType.GASTO).first().map { it.name }
        )
        settingsDao.updateCategoryIcon(TransactionType.GASTO, "Trabajo", "Work")
        assertEquals(
            "Work",
            settingsDao.observeCategories(TransactionType.GASTO).first().single().iconName
        )
        assertEquals(DefaultSettings.SAVINGS_GOAL_CENTS, AppSettings().savingsGoalCents)
    }
}
