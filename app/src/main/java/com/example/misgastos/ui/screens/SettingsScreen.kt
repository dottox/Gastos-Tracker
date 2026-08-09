package com.example.misgastos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.misgastos.R
import com.example.misgastos.data.local.Category
import com.example.misgastos.data.local.ThemeMode
import com.example.misgastos.data.local.TransactionType
import com.example.misgastos.domain.MoneyFormatter
import com.example.misgastos.domain.SettingsUiState
import com.example.misgastos.ui.components.CategoryIcon
import com.example.misgastos.ui.components.CategoryIcons

private const val MIN_DECIMAL_PLACES = 0
private const val MAX_DECIMAL_PLACES = 2

@Composable
fun SettingsScreen(
    settings: SettingsUiState,
    onBack: () -> Unit,
    onSaveSettings: (decimalPlaces: Int, savingsGoalCents: Long, themeMode: ThemeMode) -> Unit,
    onAddCategory: (TransactionType, String, String) -> Unit,
    onDeleteCategory: (TransactionType, String) -> Unit,
    onUpdateCategoryIcon: (TransactionType, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var decimalPlaces by rememberSaveable { mutableIntStateOf(settings.decimalPlaces) }
    var savingsGoal by rememberSaveable {
        mutableStateOf(
            MoneyFormatter.formatAmountInput(settings.savingsGoalCents, settings.decimalPlaces)
        )
    }
    var themeName by rememberSaveable { mutableStateOf(settings.themeMode.name) }
    var showGoalError by rememberSaveable { mutableStateOf(false) }
    var expenseCategoryInput by rememberSaveable { mutableStateOf("") }
    var incomeCategoryInput by rememberSaveable { mutableStateOf("") }

    val selectedTheme = ThemeMode.values().firstOrNull { it.name == themeName }
        ?: ThemeMode.SISTEMA
    val themeOptions = listOf(
        ThemeMode.SISTEMA to R.string.theme_system,
        ThemeMode.CLARO to R.string.theme_light,
        ThemeMode.OSCURO to R.string.theme_dark
    )

    fun currentGoalCents(): Long =
        MoneyFormatter.parseToCents(savingsGoal, decimalPlaces)
            ?.takeIf { it >= 0 }
            ?: settings.savingsGoalCents

    fun updateDecimalPlaces(value: Int) {
        val currentGoalCents = MoneyFormatter.parseToCents(savingsGoal, maxDecimalPlaces = 2)
        decimalPlaces = value.coerceIn(MIN_DECIMAL_PLACES, MAX_DECIMAL_PLACES)
        val goalCents = currentGoalCents ?: settings.savingsGoalCents
        savingsGoal = MoneyFormatter.formatAmountInput(goalCents, decimalPlaces)
        onSaveSettings(decimalPlaces, goalCents, selectedTheme)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.button_back)
                )
            }
            Text(
                text = stringResource(R.string.settings_title),
                modifier = Modifier.padding(start = 4.dp),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.settings_decimals),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { updateDecimalPlaces(decimalPlaces - 1) },
                    enabled = decimalPlaces > MIN_DECIMAL_PLACES
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = stringResource(R.string.settings_decrease_decimals)
                    )
                }
                Text(
                    text = decimalPlaces.toString(),
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(
                    onClick = { updateDecimalPlaces(decimalPlaces + 1) },
                    enabled = decimalPlaces < MAX_DECIMAL_PLACES
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.settings_increase_decimals)
                    )
                }
            }
        }

        OutlinedTextField(
            value = savingsGoal,
            onValueChange = {
                savingsGoal = it
                val parsedGoal = MoneyFormatter.parseToCents(it, decimalPlaces)
                showGoalError = it.isNotBlank() && parsedGoal == null
                parsedGoal
                    ?.takeIf { cents -> cents >= 0 }
                    ?.let { cents -> onSaveSettings(decimalPlaces, cents, selectedTheme) }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.settings_savings_goal)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = showGoalError,
            supportingText = if (showGoalError) {
                { Text(stringResource(R.string.settings_goal_error)) }
            } else {
                null
            }
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.titleMedium
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                themeOptions.forEachIndexed { index, (mode, labelRes) ->
                    SegmentedButton(
                        selected = selectedTheme == mode,
                        onClick = {
                            themeName = mode.name
                            onSaveSettings(decimalPlaces, currentGoalCents(), mode)
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = themeOptions.size
                        ),
                        icon = {},
                        label = { Text(stringResource(labelRes)) }
                    )
                }
            }
        }

        HorizontalDivider()

        CategorySettingsSection(
            title = stringResource(R.string.settings_expense_categories),
            categories = settings.expenseCategories,
            input = expenseCategoryInput,
            onInputChange = { expenseCategoryInput = it },
            onAdd = { name, iconName ->
                onAddCategory(TransactionType.GASTO, name, iconName)
                expenseCategoryInput = ""
            },
            onDelete = { onDeleteCategory(TransactionType.GASTO, it) },
            onIconChange = { name, iconName ->
                onUpdateCategoryIcon(TransactionType.GASTO, name, iconName)
            }
        )

        HorizontalDivider()

        CategorySettingsSection(
            title = stringResource(R.string.settings_income_categories),
            categories = settings.incomeCategories,
            input = incomeCategoryInput,
            onInputChange = { incomeCategoryInput = it },
            onAdd = { name, iconName ->
                onAddCategory(TransactionType.INGRESO, name, iconName)
                incomeCategoryInput = ""
            },
            onDelete = { onDeleteCategory(TransactionType.INGRESO, it) },
            onIconChange = { name, iconName ->
                onUpdateCategoryIcon(TransactionType.INGRESO, name, iconName)
            }
        )
        Spacer(modifier = Modifier.size(8.dp))
    }
}

@Composable
private fun CategorySettingsSection(
    title: String,
    categories: List<Category>,
    input: String,
    onInputChange: (String) -> Unit,
    onAdd: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onIconChange: (String, String) -> Unit
) {
    var newCategoryIcon by rememberSaveable { mutableStateOf(CategoryIcons.DEFAULT_KEY) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        categories.forEach { category ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIconPicker(
                    iconName = category.iconName,
                    categoryName = category.name,
                    onIconChange = { onIconChange(category.name, it) }
                )
                Text(
                    text = category.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                IconButton(
                    onClick = { onDelete(category.name) }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = stringResource(
                            R.string.settings_remove_category,
                            category.name
                        )
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryIconPicker(
                iconName = newCategoryIcon,
                categoryName = stringResource(R.string.settings_new_category),
                onIconChange = { newCategoryIcon = it }
            )
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.settings_new_category)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            IconButton(
                onClick = {
                    onAdd(input, newCategoryIcon)
                    newCategoryIcon = CategoryIcons.DEFAULT_KEY
                },
                enabled = input.trim().isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.settings_add_category)
                )
            }
        }
    }
}

@Composable
private fun CategoryIconPicker(
    iconName: String,
    categoryName: String,
    onIconChange: (String) -> Unit
) {
    var expanded by rememberSaveable(categoryName) { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            CategoryIcon(
                iconName = iconName,
                contentDescription = stringResource(
                    R.string.settings_change_category_icon,
                    categoryName
                )
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CategoryIcons.options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes)) },
                    leadingIcon = {
                        CategoryIcon(
                            iconName = option.key,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = {
                        onIconChange(option.key)
                        expanded = false
                    }
                )
            }
        }
    }
}
