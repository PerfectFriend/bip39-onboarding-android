package com.example.bip39onboarding.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Шифрование seed-фразы ключом, производным от PIN.
 * AES-256-GCM + PBKDF2-HMAC-SHA512 (310k итераций).
 */
object CryptoUtils {

    private const val ITERATIONS = 310_000
    private const val KEY_LENGTH = 256
    private const val GCM_TAG_LENGTH = 128
    private const val SALT_SIZE = 32
    private const val IV_SIZE = 12

    data class EncryptedPayload(
        val ciphertext: String, // Base64
        val salt: String,       // Base64
        val iv: String          // Base64
    )

    fun encrypt(mnemonic: CharArray, pin: CharArray): EncryptedPayload {
        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(pin, salt)
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val plain = String(mnemonic).toByteArray(Charsets.UTF_8)
        val encrypted = cipher.doFinal(plain)

        // Очистка
        plain.fill(0)
        key.encoded.fill(0)

        return EncryptedPayload(
            ciphertext = Base64.encodeToString(encrypted, Base64.NO_WRAP),
            salt = Base64.encodeToString(salt, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    fun decrypt(payload: EncryptedPayload, pin: CharArray): CharArray? {
        return try {
            val salt = Base64.decode(payload.salt, Base64.NO_WRAP)
            val iv = Base64.decode(payload.iv, Base64.NO_WRAP)
            val ciphertext = Base64.decode(payload.ciphertext, Base64.NO_WRAP)

            val key = deriveKey(pin, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

            val decrypted = cipher.doFinal(ciphertext)
            key.encoded.fill(0)
            String(decrypted, Charsets.UTF_8).toCharArray()
        } catch (_: Exception) {
            null
        }
    }

    fun hashPin(pin: CharArray, salt: ByteArray): String {
        val key = deriveKey(pin, salt)
        val hash = Base64.encodeToString(key.encoded, Base64.NO_WRAP)
        key.encoded.fill(0)
        return hash
    }

    private fun deriveKey(pin: CharArray, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        val spec = PBEKeySpec(pin, salt, ITERATIONS, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    /** Запрещённые простые PIN */
    fun isWeakPin(pin: String): Boolean {
        if (pin.length < 6) return true
        val weak = setOf(
            "000000", "111111", "222222", "333333", "444444", "555555",
            "666666", "777777", "888888", "999999",
            "123456", "654321", "123123", "112233", "121212"
        )
        return pin in weak || pin.all { it == pin[0] }
    }
}
