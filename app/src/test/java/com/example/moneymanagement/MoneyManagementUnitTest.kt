package com.example.moneymanagement

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyManagementUnitTest {

    @Test
    fun formatRupiah_formatsIndonesianCurrencyCorrectly() {
        assertEquals("Rp0", formatRupiah(0L))
        assertEquals("Rp20.000", formatRupiah(20_000L))
        assertEquals("Rp500.000", formatRupiah(500_000L))
        assertEquals("Rp1.500.000", formatRupiah(1_500_000L))
        assertEquals("-Rp20.000", formatRupiah(-20_000L))
    }
}
