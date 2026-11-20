package com.example.moneymanagement

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MoneyManagementRobolectricTest {

    @Test
    fun initialDashboard_showsZeroAndEmptyState() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()

        assertEquals("Rp0", activity.findViewById<TextView>(R.id.tv_net_balance).text.toString())
        assertEquals("Rp0", activity.findViewById<TextView>(R.id.tv_total_income).text.toString())
        assertEquals("Rp0", activity.findViewById<TextView>(R.id.tv_total_expense).text.toString())
        assertEquals(View.VISIBLE, activity.findViewById<LinearLayout>(R.id.ll_empty_state).visibility)
        assertEquals(
            "Belum ada transaksi",
            activity.findViewById<TextView>(R.id.tv_empty_state).text.toString()
        )
    }

    @Test
    fun addIncomeAndExpense_updatesSummaryAndRetainsAcrossRotation() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val mainActivity = controller.get()
        val shadowMain = shadowOf(mainActivity)

        // Click FAB -> AddTransactionActivity for Pemasukan Rp500.000
        mainActivity.findViewById<FloatingActionButton>(R.id.btn_add_transaction).performClick()
        val startedForIncome = shadowMain.nextStartedActivityForResult
        assertNotNull(startedForIncome)
        assertEquals(
            AddTransactionActivity::class.java.name,
            startedForIncome.intent.component?.className
        )

        val incomeResultIntent = Intent().apply {
            putExtra(IntentExtras.EXTRA_TITLE, "Gaji Bulanan")
            putExtra(IntentExtras.EXTRA_AMOUNT, 500_000L)
            putExtra(IntentExtras.EXTRA_TYPE, Transaction.TYPE_IN)
        }
        shadowMain.receiveResult(
            startedForIncome.intent,
            Activity.RESULT_OK,
            incomeResultIntent
        )

        // Click FAB -> AddTransactionActivity for Pengeluaran Rp20.000
        mainActivity.findViewById<FloatingActionButton>(R.id.btn_add_transaction).performClick()
        val startedForExpense = shadowMain.nextStartedActivityForResult
        val expenseResultIntent = Intent().apply {
            putExtra(IntentExtras.EXTRA_TITLE, "Makan Siang")
            putExtra(IntentExtras.EXTRA_AMOUNT, 20_000L)
            putExtra(IntentExtras.EXTRA_TYPE, Transaction.TYPE_OUT)
        }
        shadowMain.receiveResult(
            startedForExpense.intent,
            Activity.RESULT_OK,
            expenseResultIntent
        )

        assertEquals("Rp500.000", mainActivity.findViewById<TextView>(R.id.tv_total_income).text.toString())
        assertEquals("Rp20.000", mainActivity.findViewById<TextView>(R.id.tv_total_expense).text.toString())
        assertEquals("Rp480.000", mainActivity.findViewById<TextView>(R.id.tv_net_balance).text.toString())

        val container = mainActivity.findViewById<LinearLayout>(R.id.ll_transaction_container)
        assertEquals(View.GONE, mainActivity.findViewById<LinearLayout>(R.id.ll_empty_state).visibility)
        assertEquals(View.VISIBLE, container.visibility)
        assertEquals(2, container.childCount)

        // Test Share Summary Intent
        mainActivity.findViewById<MaterialButton>(R.id.btn_share_summary).performClick()
        val chooserIntent = shadowMain.nextStartedActivity
        assertNotNull(chooserIntent)
        assertEquals(Intent.ACTION_CHOOSER, chooserIntent.action)
        @Suppress("DEPRECATION")
        val targetShare = chooserIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertNotNull(targetShare)
        assertEquals(Intent.ACTION_SEND, targetShare?.action)
        assertEquals("text/plain", targetShare?.type)
        val sharedText = targetShare?.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        assertTrue(sharedText.contains("Total Pemasukan: Rp500.000"))
        assertTrue(sharedText.contains("Total Pengeluaran: Rp20.000"))
        assertTrue(sharedText.contains("Saldo Bersih: Rp480.000"))

        // Simulate Screen Rotation via recreate()
        controller.recreate()
        val recreated = controller.get()
        assertEquals("Rp500.000", recreated.findViewById<TextView>(R.id.tv_total_income).text.toString())
        assertEquals("Rp20.000", recreated.findViewById<TextView>(R.id.tv_total_expense).text.toString())
        assertEquals("Rp480.000", recreated.findViewById<TextView>(R.id.tv_net_balance).text.toString())
        assertEquals(2, recreated.findViewById<LinearLayout>(R.id.ll_transaction_container).childCount)

        // Click first transaction item -> opens DetailTransactionActivity with Parcelable
        recreated.findViewById<LinearLayout>(R.id.ll_transaction_container).getChildAt(0).performClick()
        val detailIntent = shadowOf(recreated).nextStartedActivity
        assertNotNull(detailIntent)
        assertEquals(
            DetailTransactionActivity::class.java.name,
            detailIntent.component?.className
        )

        val detailActivity = Robolectric.buildActivity(DetailTransactionActivity::class.java, detailIntent)
            .setup()
            .get()
        assertEquals("Makan Siang", detailActivity.findViewById<TextView>(R.id.tv_detail_title).text.toString())
        assertEquals("− Rp20.000", detailActivity.findViewById<TextView>(R.id.tv_detail_amount).text.toString())
        assertEquals("Pengeluaran (OUT)", detailActivity.findViewById<TextView>(R.id.tv_detail_type).text.toString())
        assertEquals("#002", detailActivity.findViewById<TextView>(R.id.tv_detail_id).text.toString())

        detailActivity.findViewById<MaterialButton>(R.id.btn_back).performClick()
        assertTrue(detailActivity.isFinishing)
    }

    @Test
    fun addTransactionValidation_andCancelBehavior() {
        val addActivity = Robolectric.buildActivity(AddTransactionActivity::class.java).setup().get()
        val tilTitle = addActivity.findViewById<TextInputLayout>(R.id.til_title)
        val tilAmount = addActivity.findViewById<TextInputLayout>(R.id.til_amount)
        val edtTitle = addActivity.findViewById<TextInputEditText>(R.id.edt_title)
        val edtAmount = addActivity.findViewById<TextInputEditText>(R.id.edt_amount)
        val btnSave = addActivity.findViewById<MaterialButton>(R.id.btn_save)

        // 1. Empty title & amount
        btnSave.performClick()
        assertFalse(addActivity.isFinishing)
        assertEquals("Judul transaksi wajib diisi", tilTitle.error.toString())
        assertEquals("Nominal wajib diisi", tilAmount.error.toString())

        // 2. Zero amount
        edtTitle.setText("Bonus")
        edtAmount.setText("0")
        btnSave.performClick()
        assertFalse(addActivity.isFinishing)
        assertEquals("Nominal harus lebih besar dari nol", tilAmount.error.toString())

        // 3. Valid input produces RESULT_OK
        addActivity.findViewById<RadioButton>(R.id.rb_expense).isChecked = true
        edtAmount.setText("25000")
        btnSave.performClick()
        assertTrue(addActivity.isFinishing)
        val shadowAdd = shadowOf(addActivity)
        assertEquals(Activity.RESULT_OK, shadowAdd.resultCode)
        assertEquals("Bonus", shadowAdd.resultIntent.getStringExtra(IntentExtras.EXTRA_TITLE))
        assertEquals(25_000L, shadowAdd.resultIntent.getLongExtra(IntentExtras.EXTRA_AMOUNT, 0L))
        assertEquals(Transaction.TYPE_OUT, shadowAdd.resultIntent.getStringExtra(IntentExtras.EXTRA_TYPE))
    }

    @Test
    fun addTransactionCancel_finishesWithoutSaving() {
        val addActivity = Robolectric.buildActivity(AddTransactionActivity::class.java).setup().get()
        addActivity.findViewById<MaterialButton>(R.id.btn_cancel).performClick()
        assertTrue(addActivity.isFinishing)
        assertEquals(Activity.RESULT_CANCELED, shadowOf(addActivity).resultCode)
    }
}
