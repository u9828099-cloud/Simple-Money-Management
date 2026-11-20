package com.example.moneymanagement

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.moneymanagement.databinding.ActivityMainBinding

/**
 * Dashboard utama Simple Money Management.
 * Mengelola daftar transaksi di memori (ArrayList<Transaction>), menghitung ringkasan keuangan,
 * menangani rotasi layar melalui onSaveInstanceState(), serta mencatat Activity Lifecycle.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var transactionList: ArrayList<Transaction> = ArrayList()
    private var nextTransactionId: Int = 1

    /**
     * Launcher Activity Result API untuk menerima transaksi baru dari AddTransactionActivity.
     */
    private val addTransactionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val title = data.getStringExtra(IntentExtras.EXTRA_TITLE).orEmpty().trim()
            val amount = data.getLongExtra(IntentExtras.EXTRA_AMOUNT, 0L)
            val type = data.getStringExtra(IntentExtras.EXTRA_TYPE) ?: Transaction.TYPE_OUT

            if (title.isNotEmpty() && amount > 0L) {
                val newTransaction = Transaction(
                    id = nextTransactionId++,
                    title = title,
                    amount = amount,
                    type = type,
                    date = currentFormattedDate()
                )
                transactionList.add(newTransaction)
                updateDashboardUI()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        restoreInstanceState(savedInstanceState)
        setupClickListeners()
        updateDashboardUI()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    /**
     * Menyimpan daftar transaksi dan ID berikutnya sebelum Activity dihancurkan (mis. rotasi layar).
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState - saving ${transactionList.size} transactions")
        outState.putParcelableArrayList(IntentExtras.STATE_TRANSACTIONS, transactionList)
        outState.putInt(IntentExtras.STATE_NEXT_ID, nextTransactionId)
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootMain) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) return
        val restoredList = BundleCompat.getParcelableArrayList(
            savedInstanceState,
            IntentExtras.STATE_TRANSACTIONS,
            Transaction::class.java
        )
        if (restoredList != null) {
            transactionList = restoredList
        }
        nextTransactionId = savedInstanceState.getInt(IntentExtras.STATE_NEXT_ID, 1)
    }

    private fun setupClickListeners() {
        binding.btnAddTransaction.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            addTransactionLauncher.launch(intent)
        }

        binding.btnShareSummary.setOnClickListener {
            shareFinancialSummary()
        }
    }

    /**
     * Menghitung ulang Total Pemasukan, Total Pengeluaran, dan Saldo Bersih,
     * lalu memperbarui kartu ringkasan dan daftar transaksi dinamis.
     */
    private fun updateDashboardUI() {
        val totalIncome = calculateTotalIncome()
        val totalExpense = calculateTotalExpense()
        val netBalance = totalIncome - totalExpense

        binding.tvTotalIncome.text = formatRupiah(totalIncome)
        binding.tvTotalExpense.text = formatRupiah(totalExpense)
        binding.tvNetBalance.text = formatRupiah(netBalance)

        binding.tvTransactionCount.text = if (transactionList.isEmpty()) {
            getString(R.string.label_transaction_count_empty)
        } else {
            getString(R.string.label_transaction_count_format, transactionList.size)
        }

        renderTransactionList()
    }

    private fun calculateTotalIncome(): Long {
        return transactionList.filter { it.isIncome }.sumOf { it.amount }
    }

    private fun calculateTotalExpense(): Long {
        return transactionList.filter { !it.isIncome }.sumOf { it.amount }
    }

    /**
     * Menampilkan empty state jika belum ada transaksi, atau merender TextView transaksi
     * secara dinamis ke dalam LinearLayout di dalam ScrollView.
     */
    private fun renderTransactionList() {
        binding.llTransactionContainer.removeAllViews()

        if (transactionList.isEmpty()) {
            binding.llEmptyState.visibility = View.VISIBLE
            binding.llTransactionContainer.visibility = View.GONE
            return
        }

        binding.llEmptyState.visibility = View.GONE
        binding.llTransactionContainer.visibility = View.VISIBLE

        val recentFirst = transactionList.asReversed()
        recentFirst.forEachIndexed { index, transaction ->
            val itemView = createTransactionItemView(
                transaction = transaction,
                isLastItem = index == recentFirst.lastIndex
            )
            binding.llTransactionContainer.addView(itemView)
        }
    }

    /**
     * Membuat baris tampilan transaksi secara terprogram menggunakan TextView di dalam LinearLayout.
     */
    private fun createTransactionItemView(
        transaction: Transaction,
        isLastItem: Boolean
    ): LinearLayout {
        val isIncome = transaction.isIncome
        val accentColor = ContextCompat.getColor(
            this,
            if (isIncome) R.color.income_green else R.color.expense_red
        )
        val primaryTextColor = ContextCompat.getColor(this, R.color.text_primary)
        val secondaryTextColor = ContextCompat.getColor(this, R.color.text_secondary)

        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_transaction_item)
            minimumHeight = dpToPx(64)
            setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12))
            isClickable = true
            isFocusable = true

            val rippleOut = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, rippleOut, true)
            foreground = ContextCompat.getDrawable(this@MainActivity, rippleOut.resourceId)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = if (isLastItem) 0 else dpToPx(10)
            }

            setOnClickListener {
                openTransactionDetail(transaction)
            }
        }

        // Lencana tanda + / −
        val tvBadge = TextView(this).apply {
            text = if (isIncome) "+" else "−"
            textSize = 18f
            setTextColor(accentColor)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            background = ContextCompat.getDrawable(
                this@MainActivity,
                if (isIncome) R.drawable.bg_badge_income else R.drawable.bg_badge_expense
            )
            layoutParams = LinearLayout.LayoutParams(dpToPx(40), dpToPx(40)).apply {
                marginEnd = dpToPx(12)
            }
        }

        // Kolom tengah: Nama dan tanggal transaksi
        val textColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginEnd = dpToPx(10)
            }
        }

        val tvTitle = TextView(this).apply {
            text = transaction.title
            textSize = 15f
            setTextColor(primaryTextColor)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }

        val tvDate = TextView(this).apply {
            text = transaction.date
            textSize = 12f
            setTextColor(secondaryTextColor)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(2)
            }
        }

        textColumn.addView(tvTitle)
        textColumn.addView(tvDate)

        // Nominal transaksi kanan dengan tanda + / −
        val tvAmount = TextView(this).apply {
            val signPrefix = if (isIncome) "+ " else "− "
            text = "$signPrefix${formatRupiah(transaction.amount)}"
            textSize = 15f
            setTextColor(accentColor)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.END
        }

        rowLayout.addView(tvBadge)
        rowLayout.addView(textColumn)
        rowLayout.addView(tvAmount)

        return rowLayout
    }

    /**
     * Membuka DetailTransactionActivity dengan mengirimkan satu objek Transaction Parcelable.
     */
    private fun openTransactionDetail(transaction: Transaction) {
        val intent = Intent(this, DetailTransactionActivity::class.java).apply {
            putExtra(IntentExtras.EXTRA_TRANSACTION, transaction)
        }
        startActivity(intent)
    }

    /**
     * Membagikan ringkasan keuangan aktual menggunakan Implicit Intent ACTION_SEND dan createChooser().
     */
    private fun shareFinancialSummary() {
        val totalIncome = calculateTotalIncome()
        val totalExpense = calculateTotalExpense()
        val netBalance = totalIncome - totalExpense

        val summaryText = buildString {
            appendLine("Ringkasan Keuangan")
            appendLine("Simple Money Management")
            appendLine()
            appendLine("Total Pemasukan: ${formatRupiah(totalIncome)}")
            appendLine("Total Pengeluaran: ${formatRupiah(totalExpense)}")
            append("Saldo Bersih: ${formatRupiah(netBalance)}")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_chooser_title))
            putExtra(Intent.EXTRA_TEXT, summaryText)
        }

        val chooserIntent = Intent.createChooser(sendIntent, getString(R.string.share_chooser_title))
        try {
            startActivity(chooserIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.share_chooser_title), Toast.LENGTH_SHORT).show()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    companion object {
        private const val TAG = "MainActivityLifecycle"
    }
}
