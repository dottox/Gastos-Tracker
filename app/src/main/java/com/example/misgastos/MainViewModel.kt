package com.example.misgastos

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.misgastos.data.SettingsRepository
import com.example.misgastos.data.TransactionRepository
import com.example.misgastos.data.local.AppDatabase
import com.example.misgastos.data.local.AppSettings
import com.example.misgastos.data.local.DefaultSettings
import com.example.misgastos.data.local.Transaction
import com.example.misgastos.data.local.TransactionType
import com.example.misgastos.data.local.ThemeMode
import com.example.misgastos.domain.MainUiState
import com.example.misgastos.domain.SettingsUiState
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class MainEvent {
    data object TransactionSaved : MainEvent()
    data object TransactionDeleted : MainEvent()
    data object StorageError : MainEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repository: TransactionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    companion object {
        const val DEFAULT_SAVINGS_GOAL_CENTS = DefaultSettings.SAVINGS_GOAL_CENTS
    }

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    private val appSettings = settingsRepository.observeSettings()
        .map { settings -> settings ?: AppSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = AppSettings()
        )
    private val expenseCategories = settingsRepository.observeCategories(TransactionType.GASTO)
    private val incomeCategories = settingsRepository.observeCategories(TransactionType.INGRESO)
    private val categoryIcons = combine(
        expenseCategories,
        incomeCategories
    ) { expenses, incomes ->
        mapOf(
            TransactionType.GASTO to expenses.associate { it.name to it.iconName },
            TransactionType.INGRESO to incomes.associate { it.name to it.iconName }
        )
    }

    val settingsUiState: StateFlow<SettingsUiState> = combine(
        appSettings,
        expenseCategories,
        incomeCategories
    ) { settings, expenses, incomes ->
        SettingsUiState(
            decimalPlaces = settings.decimalPlaces,
            savingsGoalCents = settings.savingsGoalCents,
            themeMode = settings.themeMode,
            expenseCategories = expenses,
            incomeCategories = incomes
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = SettingsUiState()
    )

    private val monthlyTransactions = selectedMonth.flatMapLatest { month ->
        repository.observeTransactionsForMonth(month)
    }
    private val monthlySummary = selectedMonth.flatMapLatest { month ->
        repository.observeMonthlySummary(month)
    }

    private data class MonthlyData(
        val transactions: List<Transaction>,
        val incomeCents: Long,
        val expenseCents: Long,
        val historicalSavingsCents: Long
    )

    private val monthlyData = combine(
        monthlyTransactions,
        monthlySummary
    ) { transactions, summary ->
        MonthlyData(
            transactions = transactions,
            incomeCents = summary.incomeCents,
            expenseCents = summary.expenseCents,
            historicalSavingsCents = summary.historicalSavingsCents
        )
    }

    val uiState: StateFlow<MainUiState> = combine(
        selectedMonth,
        monthlyData,
        appSettings,
        categoryIcons
    ) { month, data, settings, icons ->
        MainUiState(
            selectedMonth = month,
            transactions = data.transactions,
            incomeTotalCents = data.incomeCents,
            expenseTotalCents = data.expenseCents,
            historicalSavingsCents = data.historicalSavingsCents,
            savingsGoalCents = settings.savingsGoalCents,
            decimalPlaces = settings.decimalPlaces,
            categoryIcons = icons
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = MainUiState(
            selectedMonth = selectedMonth.value,
            savingsGoalCents = DEFAULT_SAVINGS_GOAL_CENTS
        )
    )

    private val _events = MutableSharedFlow<MainEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                settingsRepository.seedDefaults()
            } catch (_: Exception) {
                _events.emit(MainEvent.StorageError)
            }
        }
    }

    fun goToPreviousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun goToNextMonth() {
        selectedMonth.value = selectedMonth.value.plusMonths(1)
    }

    fun saveTransaction(transaction: Transaction) {
        if (
            transaction.title.isBlank() ||
            transaction.category.isBlank() ||
            transaction.amountCents <= 0
        ) {
            _events.tryEmit(MainEvent.StorageError)
            return
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (transaction.id == 0L) {
                        repository.insert(transaction)
                    } else {
                        repository.update(transaction)
                    }
                }
                _events.emit(MainEvent.TransactionSaved)
            } catch (_: Exception) {
                _events.emit(MainEvent.StorageError)
            }
        }
    }

    fun saveSettings(decimalPlaces: Int, savingsGoalCents: Long, themeMode: ThemeMode) {
        if (savingsGoalCents < 0) {
            _events.tryEmit(MainEvent.StorageError)
            return
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    settingsRepository.saveSettings(
                        decimalPlaces = decimalPlaces.coerceIn(0, 2),
                        savingsGoalCents = savingsGoalCents,
                        themeMode = themeMode
                    )
                }
            } catch (_: Exception) {
                _events.emit(MainEvent.StorageError)
            }
        }
    }

    fun addCategory(
        type: TransactionType,
        name: String,
        iconName: String = DefaultSettings.DEFAULT_CATEGORY_ICON
    ) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    settingsRepository.addCategory(type, name, iconName)
                }
            } catch (_: Exception) {
                _events.emit(MainEvent.StorageError)
            }
        }
    }

    fun updateCategoryIcon(type: TransactionType, name: String, iconName: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    settingsRepository.updateCategoryIcon(type, name, iconName)
                }
            } catch (_: Exception) {
                _events.emit(MainEvent.StorageError)
            }
        }
    }

    fun deleteCategory(type: TransactionType, name: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    settingsRepository.deleteCategory(type, name)
                }
            } catch (_: Exception) {
                _events.emit(MainEvent.StorageError)
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.delete(transaction)
                }
                _events.emit(MainEvent.TransactionDeleted)
            } catch (_: Exception) {
                _events.emit(MainEvent.StorageError)
            }
        }
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(MainViewModel::class.java)) {
                throw IllegalArgumentException("Clase de ViewModel desconocida: ${modelClass.name}")
            }

            val database = AppDatabase.getInstance(application)
            return MainViewModel(
                repository = TransactionRepository(database.transactionDao()),
                settingsRepository = SettingsRepository(database.settingsDao())
            ) as T
        }
    }
}
