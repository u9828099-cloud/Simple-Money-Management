package com.example.moneymanagement

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.moneymanagement.databinding.ActivityDetailTransactionBinding
import java.util.Locale

/**
 * Halaman detail transaksi yang menerima satu objek Transaction Parcelable dari MainActivity.
 */
class DetailTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        enableEdgeToEdge()
        binding = ActivityDetailTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        bindTransactionDetail()
        setupListeners()
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

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootDetailTransaction) { view, insets ->
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

    private fun bindTransactionDetail() {
        val transaction = IntentCompat.getParcelableExtra(
            intent,
            IntentExtras.EXTRA_TRANSACTION,
            Transaction::class.java
        )

        if (transaction == null) {
            binding.tvDetailTitle.text = getString(R.string.detail_missing_error)
            binding.tvDetailAmount.text = getString(R.string.default_zero_rupiah)
            binding.tvDetailType.text = "-"
            binding.tvDetailDate.text = "-"
            binding.tvDetailId.text = "-"
            return
        }

        val isIncome = transaction.isIncome
        val accentColor = ContextCompat.getColor(
            this,
            if (isIncome) R.color.income_green else R.color.expense_red
        )
        val badgeDrawable = ContextCompat.getDrawable(
            this,
            if (isIncome) R.drawable.bg_badge_income else R.drawable.bg_badge_expense
        )

        binding.tvDetailTitle.text = transaction.title
        binding.tvDetailDate.text = transaction.date
        binding.tvDetailId.text = String.format(Locale.US, "#%03d", transaction.id)

        val signPrefix = if (isIncome) "+ " else "− "
        binding.tvDetailAmount.text = "$signPrefix${formatRupiah(transaction.amount)}"
        binding.tvDetailAmount.setTextColor(accentColor)

        binding.tvDetailType.text = getString(
            if (isIncome) R.string.type_income_label else R.string.type_expense_label
        )
        binding.tvDetailType.setTextColor(accentColor)
        binding.tvDetailType.background = badgeDrawable
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnBackHeader.setOnClickListener {
            finish()
        }
    }

    companion object {
        private const val TAG = "DetailTransactionLifecycle"
    }
}
