package com.example.moneymanagement

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Model data transaksi keuangan pribadi yang mengimplementasikan Parcelable
 * agar dapat dikirim antar Activity melalui Intent dan disimpan di onSaveInstanceState().
 */
@Parcelize
data class Transaction(
    val id: Int,
    val title: String,
    val amount: Long,
    val type: String,
    val date: String
) : Parcelable {

    val isIncome: Boolean
        get() = type == TYPE_IN

    companion object {
        const val TYPE_IN = "IN"
        const val TYPE_OUT = "OUT"
    }
}

/**
 * Memformat nominal Long menjadi string mata uang Rupiah Indonesia (contoh: Rp0, Rp500.000).
 */
fun formatRupiah(amount: Long): String {
    val indonesiaLocale = Locale.forLanguageTag("id-ID")
    val numberFormat = NumberFormat.getNumberInstance(indonesiaLocale).apply {
        maximumFractionDigits = 0
    }
    val formattedAbsolute = numberFormat.format(abs(amount))
    return if (amount < 0L) "-Rp$formattedAbsolute" else "Rp$formattedAbsolute"
}

/**
 * Menghasilkan stempel tanggal dan waktu transaksi saat ini dalam format lokal Indonesia.
 */
fun currentFormattedDate(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
    return formatter.format(Date())
}
