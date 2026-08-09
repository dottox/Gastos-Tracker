package com.example.misgastos

import com.example.misgastos.domain.MoneyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyFormatterTest {
    @Test
    fun parsesDecimalSeparatorsAsCents() {
        assertEquals(12345L, MoneyFormatter.parseToCents("123.45"))
        assertEquals(12345L, MoneyFormatter.parseToCents("123,45"))
        assertEquals(123456L, MoneyFormatter.parseToCents("1.234,56"))
    }

    @Test
    fun rejectsInvalidAmountsAndKeepsZeroForValidation() {
        assertEquals(0L, MoneyFormatter.parseToCents("0"))
        assertNull(MoneyFormatter.parseToCents("-10"))
        assertNull(MoneyFormatter.parseToCents("12.345"))
        assertNull(MoneyFormatter.parseToCents(""))
    }

    @Test
    fun respectsConfiguredDecimalPlaces() {
        assertEquals(12345L, MoneyFormatter.parseToCents("123.45", maxDecimalPlaces = 2))
        assertNull(MoneyFormatter.parseToCents("123.45", maxDecimalPlaces = 1))
        assertEquals(12300L, MoneyFormatter.parseToCents("123.00", maxDecimalPlaces = 0))
        assertEquals("123", MoneyFormatter.formatAmountInput(12345L, decimalPlaces = 0))
        assertEquals("123.5", MoneyFormatter.formatAmountInput(12345L, decimalPlaces = 1))
    }
}
