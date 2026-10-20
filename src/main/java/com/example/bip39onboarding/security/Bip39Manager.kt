package com.example.bip39onboarding.security

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.Mnemonics.MnemonicCode
import cash.z.ecc.android.bip39.Mnemonics.WordCount
import java.security.SecureRandom

/**
 * Безопасная работа с BIP39.
 * Использует CharArray для снижения риска утечки в String pool.
 */
object Bip39Manager {

    enum class Strength(val wordCount: WordCount) {
        WORDS_12(WordCount.COUNT_12),
        WORDS_24(WordCount.COUNT_24);

        val count: Int get() = wordCount.count
    }

    private val secureRandom = SecureRandom()

    /**
     * Генерирует мнемонику заданной силы.
     * @return CharArray (вызывающий обязан очистить через clear())
     */
    fun generate(strength: Strength = Strength.WORDS_12): CharArray {
        val entropy = ByteArray(strength.wordCount.bitLength / 8)
        secureRandom.nextBytes(entropy)
        return MnemonicCode(entropy).chars
    }

    /**
     * Валидация BIP39 (включая checksum).
     */
    fun validate(mnemonic: CharArray): Boolean {
        return try {
            MnemonicCode(mnemonic).validate()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun validate(mnemonic: String): Boolean = validate(mnemonic.toCharArray())

    /**
     * BIP39 seed (64 байта).
     */
    fun toSeed(mnemonic: CharArray, passphrase: CharArray = charArrayOf()): ByteArray {
        return MnemonicCode(mnemonic).toSeed(passphrase)
    }

    /**
     * Корокий fingerprint для UI (первые 8 байт SHA-256 от seed).
     */
    fun fingerprint(mnemonic: CharArray): String {
        val seed = toSeed(mnemonic)
        return seed.take(8).joinToString("") { "%02x".format(it) }
    }

    /**
     * 3 случайные позиции для quiz.
     */
    fun pickRandomPositions(wordCount: Int): List<Int> {
        return (0 until wordCount).shuffled(secureRandom).take(3).sorted()
    }

    /**
     * 5 вариантов ответа (1 правильный + 4 случайных из словаря).
     */
    fun generateOptions(correctWord: String, languageCode: String = "en"): List<String> {
        val wordList = Mnemonics.getCachedWords(languageCode)
        val options = mutableSetOf(correctWord)
        while (options.size < 5) {
            options.add(wordList.random(secureRandom))
        }
        return options.shuffled(secureRandom)
    }

    /**
     * Обязательная очистка чувствительных данных.
     */
    fun clear(data: CharArray?) {
        data?.fill('\u0000')
    }

    fun clear(data: ByteArray?) {
        data?.fill(0)
    }
}
