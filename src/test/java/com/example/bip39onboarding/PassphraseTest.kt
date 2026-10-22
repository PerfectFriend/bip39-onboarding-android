package com.example.bip39onboarding

import com.example.bip39onboarding.security.Bip39Manager
import org.junit.Assert.*
import org.junit.Test

class PassphraseTest {

    @Test
    fun differentPassphrase_differentSeed() {
        val m = Bip39Manager.generate(Bip39Manager.Strength.WORDS_12)
        val a = Bip39Manager.toSeed(m, charArrayOf())
        val b = Bip39Manager.toSeed(m, "correct horse battery".toCharArray())
        val c = Bip39Manager.toSeed(m, "correct horse battery".toCharArray())
        assertFalse(a.contentEquals(b))
        assertTrue(b.contentEquals(c))
        Bip39Manager.clear(m)
        Bip39Manager.clear(a)
        Bip39Manager.clear(b)
        Bip39Manager.clear(c)
    }
}
