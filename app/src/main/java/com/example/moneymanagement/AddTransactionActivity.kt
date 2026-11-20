package com.example.moneymanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import com.example.moneymanagement.databinding.ActivityAddTransactionBinding

/**
 * Halaman formulir untuk menambahkan transaksi pemasukan (IN) atau pengeluaran (OUT).
 * Melakukan validasi input dan mengembalikan data melalui Explicit Intent + setResult(RESULT_OK, intent).
 */
class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        enableEdgeToEdge()
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootAddTransaction) { view, insets ->
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

    private fun setupListeners() {
        binding.edtTitle.doAfterTextChanged {
            if (!binding.tilTitle.error.isNullOrEmpty()) {
                binding.tilTitle.error = null
            }
        }

        binding.edtAmount.doAfterTextChanged {
            if (!binding.tilAmount.error.isNullOrEmpty()) {
                binding.tilAmount.error = null
            }
        }

        binding.btnSave.setOnClickListener {
            validateAndSaveTransaction()
        }

        binding.btnCancel.setOnClickListener {
            cancelAndFinish()
        }

        binding.btnBackHeader.setOnClickListener {
            cancelAndFinish()
        }
    }

    /**
     * Memvalidasi input judul dan nominal sebelum mengirim hasil kembali ke MainActivity.
     * Jika validasi gagal, pesan error ditampilkan pada kolom yang salah dan form tetap terbuka.
     */
    private fun validateAndSaveTransaction() {
        val title = binding.edtTitle.text?.toString().orEmpty().trim()
        val amountText = binding.edtAmount.text?.toString().orEmpty().trim()

        var isValid = true

        if (title.isEmpty()) {
            binding.tilTitle.error = getString(R.string.error_title_required)
            if (isValid) binding.edtTitle.requestFocus()
            isValid = false
        } else {
            binding.tilTitle.error = null
        }

        val parsedAmount: Long? = when {
            amountText.isEmpty() -> {
                binding.tilAmount.error = getString(R.string.error_amount_required)
                if (isValid) binding.edtAmount.requestFocus()
                isValid = false
                null
            }
            !amountText.all { it.isDigit() } -> {
                binding.tilAmount.error = getString(R.string.error_amount_digits_only)
                if (isValid) binding.edtAmount.requestFocus()
                isValid = false
                null
            }
            else -> {
                val numericValue = amountText.toLongOrNull()
                if (numericValue == null || numericValue <= 0L) {
                    binding.tilAmount.error = getString(R.string.error_amount_invalid)
                    if (isValid) binding.edtAmount.requestFocus()
                    isValid = false
                    null
                } else {
                    binding.tilAmount.error = null
                    numericValue
                }
            }
        }

        if (!isValid || parsedAmount == null) {
            return
        }

        val transactionType = if (binding.rbIncome.isChecked) {
            Transaction.TYPE_IN
        } else {
            Transaction.TYPE_OUT
        }

        val resultIntent = Intent().apply {
            putExtra(IntentExtras.EXTRA_TITLE, title)
            putExtra(IntentExtras.EXTRA_AMOUNT, parsedAmount)
            putExtra(IntentExtras.EXTRA_TYPE, transactionType)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun cancelAndFinish() {
        setResult(RESULT_CANCELED)
        finish()
    }

    companion object {
        private const val TAG = "AddTransactionLifecycle"
    }
}
