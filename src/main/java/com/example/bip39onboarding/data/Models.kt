package com.example.bip39onboarding.data

import com.example.bip39onboarding.security.Bip39Manager

/**
 * Результат попытки разблокировки.
 */
sealed class UnlockResult {
    data class Success(val mnemonic: CharArray) : UnlockResult()
    data object WrongPin : UnlockResult()
    data object Panic : UnlockResult()
    data object NotSetup : UnlockResult()
    data object Error : UnlockResult()
}

/**
 * Payload, который передаётся в основное приложение после успешного онбординга / unlock.
 * ВАЖНО: mnemonic должен быть очищен вызывающим кодом после использования.
 */
data class SeedPayload(
    val mnemonic: CharArray,
    val fingerprint: String,
    val wordCount: Int
) {
    fun toSeedBytes(passphrase: CharArray = charArrayOf()): ByteArray {
        return Bip39Manager.toSeed(mnemonic, passphrase)
    }

    fun clear() {
        Bip39Manager.clear(mnemonic)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SeedPayload
        return mnemonic.contentEquals(other.mnemonic) &&
                fingerprint == other.fingerprint &&
                wordCount == other.wordCount
    }

    override fun hashCode(): Int {
        var result = mnemonic.contentHashCode()
        result = 31 * result + fingerprint.hashCode()
        result = 31 * result + wordCount
        return result
    }
}

enum class OnboardingStep {
    SPLASH,
    CHOOSE,
    STRENGTH,
    DISCLAIMER,
    SHOW_MNEMONIC,
    VERIFY,
    SET_PIN,
    SET_PANIC_PIN,
    RESTORE_CHOOSE,
    RESTORE_PASTE,
    RESTORE_QR,
    RESTORE_PDF,
    DONE
}
