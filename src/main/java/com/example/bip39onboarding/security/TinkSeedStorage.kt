package com.example.bip39onboarding.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.nio.charset.StandardCharsets

class TinkSeedStorage(private val context: Context) {

    private val dataStore: DataStore<Preferences> = context.seedDataStore

    private val aead: Aead by lazy {
        AeadConfig.register()
        AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREF_FILE)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(Aead::class.java)
    }

    fun isSetup(): Boolean = runBlocking {
        dataStore.data.first()[KEY_IS_SETUP] == true
    }

    fun save(
        payload: CryptoUtils.EncryptedPayload,
        pinHash: String,
        panicPinHash: String,
        wordCount: Int,
        fingerprint: String,
        hasPassphrase: Boolean = false
    ) {
        runBlocking {
            dataStore.edit { prefs ->
                prefs[KEY_CIPHERTEXT] = seal(payload.ciphertext)
                prefs[KEY_SALT] = seal(payload.salt)
                prefs[KEY_IV] = seal(payload.iv)
                prefs[KEY_PIN_HASH] = seal(pinHash)
                prefs[KEY_PANIC_HASH] = seal(panicPinHash)
                prefs[KEY_WORD_COUNT] = wordCount
                prefs[KEY_FINGERPRINT] = fingerprint
                prefs[KEY_HAS_PASSPHRASE] = hasPassphrase
                prefs[KEY_IS_SETUP] = true
            }
        }
    }

    fun getPayload(): CryptoUtils.EncryptedPayload? = runBlocking {
        val prefs = dataStore.data.first()
        val ct = prefs[KEY_CIPHERTEXT]?.let { open(it) } ?: return@runBlocking null
        val salt = prefs[KEY_SALT]?.let { open(it) } ?: return@runBlocking null
        val iv = prefs[KEY_IV]?.let { open(it) } ?: return@runBlocking null
        CryptoUtils.EncryptedPayload(ct, salt, iv)
    }

    fun getPinHash(): String? = runBlocking { dataStore.data.first()[KEY_PIN_HASH]?.let { open(it) } }
    fun getPanicPinHash(): String? = runBlocking { dataStore.data.first()[KEY_PANIC_HASH]?.let { open(it) } }
    fun getFingerprint(): String? = runBlocking { dataStore.data.first()[KEY_FINGERPRINT] }
    fun getWordCount(): Int = runBlocking { dataStore.data.first()[KEY_WORD_COUNT] ?: 12 }
    fun hasPassphrase(): Boolean = runBlocking { dataStore.data.first()[KEY_HAS_PASSPHRASE] == true }

    fun wipe() {
        runBlocking { dataStore.edit { it.clear() } }
    }

    fun migrateFromLegacyIfNeeded() {
        if (isSetup()) return
        val legacy = SecureSeedStorage(context)
        if (!legacy.isSetup()) return
        val payload = legacy.getPayload() ?: return
        val pinHash = legacy.getPinHash() ?: return
        val panicHash = legacy.getPanicPinHash() ?: return
        save(payload, pinHash, panicHash, legacy.getWordCount(), legacy.getFingerprint() ?: "", false)
        legacy.wipe()
    }

    private fun seal(plain: String): String {
        val cipher = aead.encrypt(plain.toByteArray(StandardCharsets.UTF_8), ASSOCIATED_DATA)
        return android.util.Base64.encodeToString(cipher, android.util.Base64.NO_WRAP)
    }

    private fun open(sealed: String): String {
        val cipher = android.util.Base64.decode(sealed, android.util.Base64.NO_WRAP)
        return String(aead.decrypt(cipher, ASSOCIATED_DATA), StandardCharsets.UTF_8)
    }

    companion object {
        private val Context.seedDataStore by preferencesDataStore(name = "bip39_seed_store")
        private const val KEYSET_NAME = "bip39_tink_keyset"
        private const val PREF_FILE = "bip39_tink_prefs"
        private const val MASTER_KEY_URI = "android-keystore://bip39_tink_master_key"
        private val ASSOCIATED_DATA = "bip39-onboarding".toByteArray(StandardCharsets.UTF_8)
        private val KEY_IS_SETUP = booleanPreferencesKey("is_setup")
        private val KEY_CIPHERTEXT = stringPreferencesKey("ciphertext")
        private val KEY_SALT = stringPreferencesKey("salt")
        private val KEY_IV = stringPreferencesKey("iv")
        private val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        private val KEY_PANIC_HASH = stringPreferencesKey("panic_pin_hash")
        private val KEY_WORD_COUNT = intPreferencesKey("word_count")
        private val KEY_FINGERPRINT = stringPreferencesKey("fingerprint")
        private val KEY_HAS_PASSPHRASE = booleanPreferencesKey("has_passphrase")
    }
}
