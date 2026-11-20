package com.example.moneymanagement

/**
 * Konstanta key untuk Explicit Intent extras dan Bundle onSaveInstanceState.
 */
object IntentExtras {
    const val EXTRA_TRANSACTION = "extra_transaction"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_AMOUNT = "extra_amount"
    const val EXTRA_TYPE = "extra_type"

    const val STATE_TRANSACTIONS = "state_transactions"
    const val STATE_NEXT_ID = "state_next_id"
}
