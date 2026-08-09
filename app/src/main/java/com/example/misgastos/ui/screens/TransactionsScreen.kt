package com.example.misgastos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.misgastos.R
import com.example.misgastos.data.local.Transaction
import com.example.misgastos.data.local.TransactionType
import com.example.misgastos.domain.MainUiState
import com.example.misgastos.domain.MoneyFormatter
import com.example.misgastos.ui.components.CategoryIcons
import com.example.misgastos.ui.components.MonthSelector
import com.example.misgastos.ui.components.TransactionCard

@Composable
fun GastosScreen(
    uiState: MainUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    TransactionsScreen(
        uiState = uiState,
        type = TransactionType.GASTO,
        onPreviousMonth = onPreviousMonth,
        onNextMonth = onNextMonth,
        onAdd = onAdd,
        onEdit = onEdit,
        onDelete = onDelete
    )
}

@Composable
fun IngresosScreen(
    uiState: MainUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    TransactionsScreen(
        uiState = uiState,
        type = TransactionType.INGRESO,
        onPreviousMonth = onPreviousMonth,
        onNextMonth = onNextMonth,
        onAdd = onAdd,
        onEdit = onEdit,
        onDelete = onDelete
    )
}

@Composable
private fun TransactionsScreen(
    uiState: MainUiState,
    type: TransactionType,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    val isIncome = type == TransactionType.INGRESO
    val transactions = remember(uiState.transactions, type) {
        uiState.transactions.filter { it.type == type }
    }
    val total = if (isIncome) uiState.incomeTotalCents else uiState.expenseTotalCents
    val accent = if (isIncome) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }
    val pageTitle = stringResource(if (isIncome) R.string.page_income else R.string.page_expenses)
    val addDescription = stringResource(if (isIncome) R.string.add_income else R.string.add_expense)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        MonthSelector(
            month = uiState.selectedMonth,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = pageTitle,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = MoneyFormatter.formatCurrency(total, uiState.decimalPlaces),
                style = MaterialTheme.typography.titleLarge,
                color = accent
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            if (transactions.isEmpty()) {
                EmptyTransactions(
                    type = type,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = transactions,
                        key = { transaction -> transaction.id }
                    ) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            decimalPlaces = uiState.decimalPlaces,
                            categoryIcon = uiState.categoryIcons[type]
                                ?.get(transaction.category)
                                ?: CategoryIcons.DEFAULT_KEY,
                            onEdit = { onEdit(transaction) },
                            onDelete = { onDelete(transaction) }
                        )
                    }
                }
            }
            FloatingActionButton(
                onClick = onAdd,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 12.dp),
                containerColor = accent,
                contentColor = if (isIncome) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSecondary
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = addDescription
                )
            }
        }
    }
}

@Composable
private fun EmptyTransactions(
    type: TransactionType,
    modifier: Modifier = Modifier
) {
    val message = stringResource(
        if (type == TransactionType.INGRESO) {
            R.string.empty_income
        } else {
            R.string.empty_expenses
        }
    )
    Text(
        text = message,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge
    )
}
