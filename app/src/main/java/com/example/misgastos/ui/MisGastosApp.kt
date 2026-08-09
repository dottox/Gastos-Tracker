package com.example.misgastos.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.misgastos.MainEvent
import com.example.misgastos.MainViewModel
import com.example.misgastos.R
import com.example.misgastos.data.local.Transaction
import com.example.misgastos.data.local.TransactionType
import com.example.misgastos.ui.components.AppNotification
import com.example.misgastos.ui.components.ConfirmDeleteDialog
import com.example.misgastos.ui.components.TransactionForm
import com.example.misgastos.ui.screens.GastosScreen
import com.example.misgastos.ui.screens.IngresosScreen
import com.example.misgastos.ui.screens.SettingsScreen
import com.example.misgastos.ui.screens.StatusScreen
import com.example.misgastos.ui.theme.MisGastosTheme
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PAGE_STATUS = 0
private const val PAGE_EXPENSES = 1
private const val PAGE_INCOME = 2
private const val PAGE_COUNT = 3
private val PAGE_LABELS = intArrayOf(
    R.string.page_status,
    R.string.page_expenses,
    R.string.page_income
)

private data class NotificationState(
    val id: Long,
    val message: String,
    val isError: Boolean
)

@Composable
fun MisGastosApp(viewModel: MainViewModel) {
    val settingsState by viewModel.settingsUiState.collectAsStateWithLifecycle()

    MisGastosTheme(themeMode = settingsState.themeMode) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
        val coroutineScope = rememberCoroutineScope()
        val resources = LocalResources.current
        val haptic = LocalHapticFeedback.current
        var formTransaction by remember { mutableStateOf<Transaction?>(null) }
        var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
        var showSettings by rememberSaveable { mutableStateOf(false) }
        var notification by remember { mutableStateOf<NotificationState?>(null) }
        var notificationId by remember { mutableLongStateOf(0L) }

        BackHandler(enabled = showSettings) { showSettings = false }

        LaunchedEffect(viewModel) {
            viewModel.events.collect { event ->
                val message = when (event) {
                    MainEvent.TransactionSaved -> R.string.transaction_saved
                    MainEvent.TransactionDeleted -> R.string.transaction_deleted
                    MainEvent.StorageError -> R.string.storage_error
                }
                notificationId += 1
                notification = NotificationState(
                    id = notificationId,
                    message = resources.getString(message),
                    isError = event == MainEvent.StorageError
                )
            }
        }

        LaunchedEffect(notification?.id) {
            if (notification != null) {
                delay(2_500)
                notification = null
            }
        }

        fun openNewTransaction(type: TransactionType) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            formTransaction = Transaction(
                title = "",
                amountCents = 0,
                category = "",
                date = defaultDateFor(uiState.selectedMonth),
                type = type
            )
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            if (showSettings) {
                SettingsScreen(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                        .padding(innerPadding),
                    settings = settingsState,
                    onBack = { showSettings = false },
                    onSaveSettings = viewModel::saveSettings,
                    onAddCategory = viewModel::addCategory,
                    onDeleteCategory = viewModel::deleteCategory,
                    onUpdateCategoryIcon = viewModel::updateCategoryIcon
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, top = 12.dp, end = 12.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = stringResource(R.string.app_name),
                                    modifier = Modifier.padding(start = 10.dp),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            IconButton(onClick = { showSettings = true }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = stringResource(R.string.open_settings)
                                )
                            }
                        }
                        PrimaryTabRow(
                            selectedTabIndex = pagerState.currentPage,
                            containerColor = MaterialTheme.colorScheme.background
                        ) {
                            PAGE_LABELS.forEachIndexed { index, labelRes ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                    },
                                    text = { Text(stringResource(labelRes)) }
                                )
                            }
                        }
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f)
                        ) { page ->
                            when (page) {
                                PAGE_STATUS -> StatusScreen(
                                    uiState = uiState,
                                    onPreviousMonth = viewModel::goToPreviousMonth,
                                    onNextMonth = viewModel::goToNextMonth
                                )

                                PAGE_EXPENSES -> GastosScreen(
                                    uiState = uiState,
                                    onPreviousMonth = viewModel::goToPreviousMonth,
                                    onNextMonth = viewModel::goToNextMonth,
                                    onAdd = { openNewTransaction(TransactionType.GASTO) },
                                    onEdit = { formTransaction = it },
                                    onDelete = { transactionToDelete = it }
                                )

                                PAGE_INCOME -> IngresosScreen(
                                    uiState = uiState,
                                    onPreviousMonth = viewModel::goToPreviousMonth,
                                    onNextMonth = viewModel::goToNextMonth,
                                    onAdd = { openNewTransaction(TransactionType.INGRESO) },
                                    onEdit = { formTransaction = it },
                                    onDelete = { transactionToDelete = it }
                                )
                            }
                        }
                    }
                    notification?.let { currentNotification ->
                        AppNotification(
                            message = currentNotification.message,
                            isError = currentNotification.isError,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 92.dp, bottom = 12.dp)
                        )
                    }
                }
            }
        }

        formTransaction?.let { transaction ->
            TransactionForm(
                transaction = transaction,
                categories = if (transaction.type == TransactionType.INGRESO) {
                    settingsState.incomeCategories.map { it.name }
                } else {
                    settingsState.expenseCategories.map { it.name }
                },
                decimalPlaces = settingsState.decimalPlaces,
                onDismiss = { formTransaction = null },
                onSave = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.saveTransaction(it)
                    formTransaction = null
                }
            )
        }

        transactionToDelete?.let { transaction ->
            ConfirmDeleteDialog(
                transaction = transaction,
                onDismiss = { transactionToDelete = null },
                onConfirm = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.deleteTransaction(transaction)
                    transactionToDelete = null
                }
            )
        }
    }
}

private fun defaultDateFor(month: YearMonth): Long {
    val today = LocalDate.now()
    return if (YearMonth.from(today) == month) {
        today.toEpochDay()
    } else {
        month.atDay(1).toEpochDay()
    }
}
