package com.example.misgastos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.example.misgastos.R
import com.example.misgastos.data.local.Transaction
import com.example.misgastos.data.local.TransactionType
import com.example.misgastos.domain.MoneyFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val ZERO_AMOUNT = Regex("^0+(?:[.,]0+)?$")
private val SPANISH_LOCALE = Locale.forLanguageTag("es-ES")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionForm(
    transaction: Transaction,
    categories: List<String>,
    decimalPlaces: Int,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    val isEditing = transaction.id != 0L
    val titleText = if (transaction.type == TransactionType.INGRESO) {
        if (isEditing) R.string.edit_income else R.string.new_income
    } else {
        if (isEditing) R.string.edit_expense else R.string.new_expense
    }
    var title by rememberSaveable(transaction.id) { mutableStateOf(transaction.title) }
    var amount by rememberSaveable(transaction.id) {
        mutableStateOf(
            if (transaction.amountCents == 0L) {
                ""
            } else {
                MoneyFormatter.formatAmountInput(transaction.amountCents, decimalPlaces)
            }
        )
    }
    var category by rememberSaveable(transaction.id) { mutableStateOf(transaction.category) }
    var date by rememberSaveable(transaction.id) { mutableLongStateOf(transaction.date) }
    var showErrors by rememberSaveable(transaction.id) { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var categoryFieldWidth by remember { mutableIntStateOf(0) }

    val parsedAmount = remember(amount, decimalPlaces) {
        MoneyFormatter.parseToCents(amount, decimalPlaces)
    }
    val dateFormatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(SPANISH_LOCALE)
    }
    val selectedDate = LocalDate.ofEpochDay(date)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 24.dp)
        ) {
            Text(
                text = stringResource(titleText),
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.size(20.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_title)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                isError = showErrors && title.isBlank(),
                supportingText = if (showErrors && title.isBlank()) {
                    { Text(stringResource(R.string.required_field)) }
                } else {
                    null
                }
            )
            Spacer(modifier = Modifier.size(10.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = if (it.trim().matches(ZERO_AMOUNT)) "" else it
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.field_amount)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = showErrors && (parsedAmount == null || parsedAmount <= 0),
                supportingText = if (showErrors && (parsedAmount == null || parsedAmount <= 0)) {
                    {
                        Text(
                            stringResource(
                                if (parsedAmount == null) {
                                    R.string.invalid_amount
                                } else {
                                    R.string.positive_amount
                                }
                            )
                        )
                    }
                } else {
                    null
                }
            )
            Spacer(modifier = Modifier.size(10.dp))
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { categoryFieldWidth = it.size.width }
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    label = { Text(stringResource(R.string.field_category)) },
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            imageVector = if (categoryExpanded) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = null
                        )
                    },
                    isError = showErrors && category.isBlank(),
                    supportingText = if (showErrors && category.isBlank()) {
                        { Text(stringResource(R.string.required_field)) }
                    } else {
                        null
                    }
                )
                if (categoryExpanded && categories.isNotEmpty()) {
                    Popup(
                        popupPositionProvider = AboveAnchorPositionProvider,
                        onDismissRequest = { categoryExpanded = false },
                        properties = PopupProperties(
                            focusable = false,
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true
                        )
                    ) {
                        val fieldWidth = with(androidx.compose.ui.platform.LocalDensity.current) {
                            categoryFieldWidth.toDp()
                        }
                        Surface(
                            modifier = if (categoryFieldWidth > 0) {
                                Modifier.width(fieldWidth)
                            } else {
                                Modifier.fillMaxWidth()
                            },
                            shape = androidx.compose.material3.MaterialTheme.shapes.extraSmall,
                            tonalElevation = 3.dp,
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .heightIn(max = 280.dp)
                                    .verticalScroll(rememberScrollState())
                                    .padding(vertical = 8.dp)
                            ) {
                                categories.forEach { option ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            category = option
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.size(10.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedDate.format(dateFormatter),
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    label = { Text(stringResource(R.string.field_date)) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = stringResource(R.string.open_date_picker)
                        )
                    },
                    singleLine = true
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            role = Role.Button,
                            onClick = { showDatePicker = true }
                        )
                )
            }
            Spacer(modifier = Modifier.size(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
                Button(
                    onClick = {
                        showErrors = true
                        if (title.isNotBlank() &&
                            category.isNotBlank() &&
                            parsedAmount != null &&
                            parsedAmount > 0
                        ) {
                            onSave(
                                transaction.copy(
                                    title = title.trim(),
                                    amountCents = parsedAmount,
                                    category = category.trim(),
                                    date = date
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.button_save))
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.toUtcMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { date = it.toEpochDay() }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.button_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private object AboveAnchorPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val x = if (layoutDirection == LayoutDirection.Ltr) {
            anchorBounds.left
        } else {
            anchorBounds.right - popupContentSize.width
        }
        val y = anchorBounds.top - popupContentSize.height
        val maxY = (windowSize.height - popupContentSize.height).coerceAtLeast(0)

        return IntOffset(
            x = x.coerceIn(0, maxX),
            y = y.coerceAtLeast(0).coerceAtMost(maxY)
        )
    }
}

private fun Long.toUtcMillis(): Long =
    LocalDate.ofEpochDay(this)
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

private fun Long.toEpochDay(): Long =
    Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .toEpochDay()
