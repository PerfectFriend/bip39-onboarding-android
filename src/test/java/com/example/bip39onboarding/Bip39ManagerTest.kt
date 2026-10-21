package com.example.bip39onboarding

import com.example.bip39onboarding.security.Bip39Manager
import com.example.bip39onboarding.security.CryptoUtils
import org.junit.Assert.*
import org.junit.Test

class Bip39ManagerTest {

    @Test
    fun generate12_validChecksum() {
        val m = Bip39Manager.generate(Bip39Manager.Strength.WORDS_12)
        assertTrue(Bip39Manager.validate(m))
        assertEquals(12, String(m).split(" ").size)
        Bip39Manager.clear(m)
    }

    @Test
    fun generate24_validChecksum() {
        val m = Bip39Manager.generate(Bip39Manager.Strength.WORDS_24)
        assertTrue(Bip39Manager.validate(m))
        assertEquals(24, String(m).split(" ").size)
        Bip39Manager.clear(m)
    }

    @Test
    fun invalidMnemonic_failsValidation() {
        assertFalse(Bip39Manager.validate("abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon aboutx".toCharArray()))
        assertFalse(Bip39Manager.validate("not a real phrase".toCharArray()))
    }

    @Test
    fun fingerprint_stable() {
        val m = Bip39Manager.generate(Bip39Manager.Strength.WORDS_12)
        val a = Bip39Manager.fingerprint(m)
        val b = Bip39Manager.fingerprint(m)
        assertEquals(a, b)
        assertEquals(16, a.length)
        Bip39Manager.clear(m)
    }

    @Test
    fun encryptDecrypt_roundTrip() {
        val m = Bip39Manager.generate(Bip39Manager.Strength.WORDS_12)
        val pin = "847291".toCharArray()
        val payload = CryptoUtils.encrypt(m, pin)
        val decrypted = CryptoUtils.decrypt(payload, pin)
        assertNotNull(decrypted)
        assertArrayEquals(m, decrypted)
        Bip39Manager.clear(m)
        Bip39Manager.clear(decrypted)
        pin.fill('\u0000')
    }

    @Test
    fun wrongPin_decryptFails() {
        val m = Bip39Manager.generate(Bip39Manager.Strength.WORDS_12)
        val pin = "847291".toCharArray()
        val payload = CryptoUtils.encrypt(m, pin)
        val bad = CryptoUtils.decrypt(payload, "000000".toCharArray())
        assertNull(bad)
        Bip39Manager.clear(m)
        pin.fill('\u0000')
    }

    @Test
    fun weakPins_detected() {
        assertTrue(CryptoUtils.isWeakPin("000000"))
        assertTrue(CryptoUtils.isWeakPin("123456"))
        assertTrue(CryptoUtils.isWeakPin("111111"))
        assertTrue(CryptoUtils.isWeakPin("12345"))
        assertFalse(CryptoUtils.isWeakPin("847291"))
    }

    @Test
    fun pickRandomPositions_countAndRange() {
        val positions = Bip39Manager.pickRandomPositions(12)
        assertEquals(3, positions.size)
        assertEquals(positions.distinct().size, positions.size)
        assertTrue(positions.all { it in 0 until 12 })
    }
}
