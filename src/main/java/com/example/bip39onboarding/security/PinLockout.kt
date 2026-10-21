package com.example.bip39onboarding.security

import android.content.Context
import android.content.SharedPreferences
import kotlin.math.min
import kotlin.math.pow

/**
 * Exponential backoff after failed PIN attempts.
 * Attempt 1–4: no delay. From 5th: 2^(n-4) seconds, capped at 5 minutes.
 */
class PinLockout(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun remainingLockMs(): Long {
        val until = prefs.getLong(KEY_LOCKED_UNTIL, 0L)
        return (until - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun isLocked(): Boolean = remainingLockMs() > 0

    fun registerFailure() {
        val failures = prefs.getInt(KEY_FAILURES, 0) + 1
        prefs.edit().putInt(KEY_FAILURES, failures).apply()
        if (failures >= FAILURES_BEFORE_LOCK) {
            val exponent = (failures - FAILURES_BEFORE_LOCK + 1).coerceAtMost(8)
            val delaySec = min(MAX_LOCK_SEC, 2.0.pow(exponent.toDouble()).toLong())
            prefs.edit()
                .putLong(KEY_LOCKED_UNTIL, System.currentTimeMillis() + delaySec * 1000L)
                .apply()
        }
    }

    fun registerSuccess() {
        prefs.edit()
            .putInt(KEY_FAILURES, 0)
            .putLong(KEY_LOCKED_UNTIL, 0L)
            .apply()
    }

    fun failureCount(): Int = prefs.getInt(KEY_FAILURES, 0)

    companion object {
        private const val PREFS = "bip39_pin_lockout"
        private const val KEY_FAILURES = "failures"
        private const val KEY_LOCKED_UNTIL = "locked_until"
        private const val FAILURES_BEFORE_LOCK = 5
        private const val MAX_LOCK_SEC = 300L
    }
}
