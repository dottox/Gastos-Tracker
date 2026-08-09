package com.example.misgastos.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object MoneyFormatter {
    private val validAmount = Regex("^\\d+(?:[.,]\\d{1,2})?$")
    private val currencyFormatters = ThreadLocal.withInitial { mutableMapOf<Int, NumberFormat>() }

    fun parseToCents(input: String, maxDecimalPlaces: Int = 2): Long? {
        val compact = input.trim().replace(" ", "")
        if (compact.isEmpty()) return null

        val lastComma = compact.lastIndexOf(',')
        val lastDot = compact.lastIndexOf('.')
        val normalized = when {
            lastComma >= 0 && lastDot >= 0 && lastComma > lastDot ->
                compact.replace(".", "").replace(',', '.')

            lastComma >= 0 && lastDot >= 0 ->
                compact.replace(",", "")

            else -> compact.replace(',', '.')
        }

        if (!validAmount.matches(normalized)) return null
        val decimalPlaces = maxDecimalPlaces.coerceIn(0, 2)

        return runCatching {
            val amount = BigDecimal(normalized)
            if (amount.stripTrailingZeros().scale() > decimalPlaces) return@runCatching null

            amount
                .setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .longValueExact()
        }.getOrNull()
    }

    fun formatCurrency(cents: Long, decimalPlaces: Int = 2): String {
        val places = decimalPlaces.coerceIn(0, 2)
        val formatters = currencyFormatters.get()
            ?: mutableMapOf<Int, NumberFormat>().also(currencyFormatters::set)
        val formatter = formatters.getOrPut(places) {
            NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
                minimumFractionDigits = places
                maximumFractionDigits = places
            }
        }
        return formatter.format(
            BigDecimal.valueOf(cents, 2).setScale(places, RoundingMode.HALF_UP)
        )
    }

    fun formatAmountInput(cents: Long, decimalPlaces: Int = 2): String {
        val places = decimalPlaces.coerceIn(0, 2)
        return BigDecimal.valueOf(cents, 2)
            .setScale(places, RoundingMode.HALF_UP)
            .toPlainString()
    }
}
