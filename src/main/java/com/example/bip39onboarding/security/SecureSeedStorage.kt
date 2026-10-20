package com.example.bip39onboarding.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Безопасное хранение зашифрованного seed.
 * Примечание: EncryptedSharedPreferences deprecated → в будущем мигрировать на DataStore + Tink.
 */
class SecureSeedStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_seed_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun isSetup(): Boolean = prefs.getBoolean(KEY_IS_SETUP, false)

    fun save(
        payload: CryptoUtils.EncryptedPayload,
        pinHash: String,
        panicPinHash: String,
        wordCount: Int,
        fingerprint: String
    ) {
        prefs.edit()
            .putString(KEY_CIPHERTEXT, payload.ciphertext)
            .putString(KEY_SALT, payload.salt)
            .putString(KEY_IV, payload.iv)
            .putString(KEY_PIN_HASH, pinHash)
            .putString(KEY_PANIC_HASH, panicPinHash)
            .putInt(KEY_WORD_COUNT, wordCount)
            .putString(KEY_FINGERPRINT, fingerprint)
            .putBoolean(KEY_IS_SETUP, true)
            .apply()
    }

    fun getPayload(): CryptoUtils.EncryptedPayload? {
        val ct = prefs.getString(KEY_CIPHERTEXT, null) ?: return null
        val salt = prefs.getString(KEY_SALT, null) ?: return null
        val iv = prefs.getString(KEY_IV, null) ?: return null
        return CryptoUtils.EncryptedPayload(ct, salt, iv)
    }

    fun getPinHash(): String? = prefs.getString(KEY_PIN_HASH, null)
    fun getPanicPinHash(): String? = prefs.getString(KEY_PANIC_HASH, null)
    fun getFingerprint(): String? = prefs.getString(KEY_FINGERPRINT, null)
    fun getWordCount(): Int = prefs.getInt(KEY_WORD_COUNT, 12)

    /** Полный wipe (используется Panic PIN) */
    fun wipe() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_IS_SETUP = "is_setup"
        private const val KEY_CIPHERTEXT = "ciphertext"
        private const val KEY_SALT = "salt"
        private const val KEY_IV = "iv"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PANIC_HASH = "panic_pin_hash"
        private const val KEY_WORD_COUNT = "word_count"
        private const val KEY_FINGERPRINT = "fingerprint"
    }
}
