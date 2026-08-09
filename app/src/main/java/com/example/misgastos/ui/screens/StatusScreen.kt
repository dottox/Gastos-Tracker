package com.example.misgastos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.misgastos.R
import com.example.misgastos.data.local.Transaction
import com.example.misgastos.data.local.TransactionType
import com.example.misgastos.domain.MainUiState
import com.example.misgastos.domain.MoneyFormatter
import com.example.misgastos.ui.components.CategoryIcon
import com.example.misgastos.ui.components.CategoryIcons
import com.example.misgastos.ui.components.MonthSelector
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SPANISH_LOCALE = Locale.forLanguageTag("es-ES")

@Composable
fun StatusScreen(
    uiState: MainUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<CategorySummary?>(null) }
    val incomeCategories = remember(uiState.transactions, uiState.categoryIcons) {
        buildCategorySummaries(
            uiState.transactions,
            TransactionType.INGRESO,
            uiState.categoryIcons[TransactionType.INGRESO].orEmpty()
        )
    }
    val expenseCategories = remember(uiState.transactions, uiState.categoryIcons) {
        buildCategorySummaries(
            uiState.transactions,
            TransactionType.GASTO,
            uiState.categoryIcons[TransactionType.GASTO].orEmpty()
        )
    }
    val monthlySavings = uiState.monthlySavingsCents
    val savingsColor = if (monthlySavings >= 0) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }
    val goal = uiState.savingsGoalCents.coerceAtLeast(1)
    val progress = (monthlySavings.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        MonthSelector(
            month = uiState.selectedMonth,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth
        )
        Text(
            text = stringResource(R.string.page_status),
            style = MaterialTheme.typography.headlineSmall
        )
        MonthlySummaryCard(uiState = uiState, savingsColor = savingsColor)
        CategoryBreakdownCard(
            incomeCategories = incomeCategories,
            expenseCategories = expenseCategories,
            decimalPlaces = uiState.decimalPlaces,
            onCategoryClick = { selectedCategory = it }
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.summary_month_savings),
                        modifier = Modifier.padding(start = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = MoneyFormatter.formatCurrency(monthlySavings, uiState.decimalPlaces),
                    style = MaterialTheme.typography.headlineSmall,
                    color = savingsColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.summary_goal),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = MoneyFormatter.formatCurrency(
                            uiState.savingsGoalCents,
                            uiState.decimalPlaces
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.savings_progress,
                        MoneyFormatter.formatCurrency(
                            monthlySavings.coerceAtLeast(0),
                            uiState.decimalPlaces
                        ),
                        MoneyFormatter.formatCurrency(
                            uiState.savingsGoalCents,
                            uiState.decimalPlaces
                        )
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.summary_lifetime_savings),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = MoneyFormatter.formatCurrency(
                            uiState.historicalSavingsCents,
                            uiState.decimalPlaces
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    selectedCategory?.let { category ->
        CategoryTransactionsDialog(
            summary = category,
            decimalPlaces = uiState.decimalPlaces,
            onDismiss = { selectedCategory = null }
        )
    }
}

private data class CategorySummary(
    val category: String,
    val type: TransactionType,
    val iconName: String,
    val totalCents: Long,
    val transactions: List<Transaction>
)

private fun buildCategorySummaries(
    transactions: List<Transaction>,
    type: TransactionType,
    iconNames: Map<String, String>
): List<CategorySummary> = transactions
    .filter { it.type == type }
    .groupBy { it.category }
    .map { (category, categoryTransactions) ->
        CategorySummary(
            category = category,
            type = type,
            iconName = iconNames[category] ?: CategoryIcons.DEFAULT_KEY,
            totalCents = categoryTransactions.sumOf { it.amountCents },
            transactions = categoryTransactions.sortedByDescending { it.date }
        )
    }
    .sortedByDescending { it.totalCents }

@Composable
private fun CategoryBreakdownCard(
    incomeCategories: List<CategorySummary>,
    expenseCategories: List<CategorySummary>,
    decimalPlaces: Int,
    onCategoryClick: (CategorySummary) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.summary_category_breakdown),
                style = MaterialTheme.typography.titleMedium
            )
            CategorySummarySection(
                title = stringResource(R.string.summary_income_categories),
                categories = incomeCategories,
                decimalPlaces = decimalPlaces,
                accent = MaterialTheme.colorScheme.primary,
                onCategoryClick = onCategoryClick
            )
            HorizontalDivider()
            CategorySummarySection(
                title = stringResource(R.string.summary_expense_categories),
                categories = expenseCategories,
                decimalPlaces = decimalPlaces,
                accent = MaterialTheme.colorScheme.secondary,
                onCategoryClick = onCategoryClick
            )
        }
    }
}

@Composable
private fun CategorySummarySection(
    title: String,
    categories: List<CategorySummary>,
    decimalPlaces: Int,
    accent: androidx.compose.ui.graphics.Color,
    onCategoryClick: (CategorySummary) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (categories.isEmpty()) {
            Text(
                text = stringResource(R.string.summary_no_category_transactions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            categories.forEach { summary ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            role = Role.Button,
                            onClick = { onCategoryClick(summary) }
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryIcon(
                            iconName = summary.iconName,
                            contentDescription = summary.category,
                            modifier = Modifier.size(32.dp),
                            tint = accent
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = summary.category,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = pluralStringResource(
                                    R.plurals.summary_transaction_count,
                                    summary.transactions.size,
                                    summary.transactions.size
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = (if (summary.type == TransactionType.INGRESO) "+" else "-") +
                                MoneyFormatter.formatCurrency(summary.totalCents, decimalPlaces),
                            style = MaterialTheme.typography.titleMedium,
                            color = accent
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTransactionsDialog(
    summary: CategorySummary,
    decimalPlaces: Int,
    onDismiss: () -> Unit
) {
    val accent = if (summary.type == TransactionType.INGRESO) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.summary_category_transactions, summary.category))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(
                        R.string.summary_category_total,
                        MoneyFormatter.formatCurrency(summary.totalCents, decimalPlaces)
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = accent
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = summary.transactions,
                        key = { transaction -> transaction.id }
                    ) { transaction ->
                        CategoryTransactionRow(
                            transaction = transaction,
                            decimalPlaces = decimalPlaces,
                            accent = accent
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.summary_close))
            }
        }
    )
}

@Composable
private fun CategoryTransactionRow(
    transaction: Transaction,
    decimalPlaces: Int,
    accent: androidx.compose.ui.graphics.Color
) {
    val dateText = remember(transaction.date) {
        LocalDate.ofEpochDay(transaction.date).format(
            DateTimeFormatter.ofPattern("d MMM", SPANISH_LOCALE)
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = (if (transaction.type == TransactionType.INGRESO) "+" else "-") +
                MoneyFormatter.formatCurrency(transaction.amountCents, decimalPlaces),
            style = MaterialTheme.typography.bodyLarge,
            color = accent
        )
    }
}

@Composable
private fun MonthlySummaryCard(
    uiState: MainUiState,
    savingsColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.summary_this_month),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SummaryMetric(
                    label = stringResource(R.string.summary_income),
                    amount = MoneyFormatter.formatCurrency(
                        uiState.incomeTotalCents,
                        uiState.decimalPlaces
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.Payments,
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    label = stringResource(R.string.summary_expenses),
                    amount = MoneyFormatter.formatCurrency(
                        uiState.expenseTotalCents,
                        uiState.decimalPlaces
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f)
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.summary_balance),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = MoneyFormatter.formatCurrency(
                        uiState.monthlySavingsCents,
                        uiState.decimalPlaces
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = savingsColor
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    amount: String,
    color: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Text(
                text = label,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.titleLarge,
            color = color
        )
    }
}
