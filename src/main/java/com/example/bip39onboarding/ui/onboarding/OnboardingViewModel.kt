package com.example.bip39onboarding.ui.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bip39onboarding.data.OnboardingStep
import com.example.bip39onboarding.data.SeedPayload
import com.example.bip39onboarding.security.Bip39Manager
import com.example.bip39onboarding.security.CryptoUtils
import com.example.bip39onboarding.security.SecureSeedStorage
import android.util.Base64

class OnboardingViewModel(
    private val storage: SecureSeedStorage
) : ViewModel() {

    var step by mutableStateOf(OnboardingStep.SPLASH)
        private set

    var strength by mutableStateOf(Bip39Manager.Strength.WORDS_12)
        private set

    var mnemonic: CharArray? = null
        private set

    var acceptedDisclaimer by mutableStateOf(false)

    var verifyPositions: List<Int> = emptyList()
        private set

    var verifyOptions: Map<Int, List<String>> = emptyMap()
        private set

    var verifyAttempts by mutableIntStateOf(0)
        private set

    var errorMessage by mutableStateOf<String?>(null)

    fun goTo(step: OnboardingStep) {
        this.step = step
        errorMessage = null
    }

    fun chooseCreate() = goTo(OnboardingStep.STRENGTH)
    fun chooseRestore() = goTo(OnboardingStep.RESTORE_CHOOSE)

    fun setStrength(s: Bip39Manager.Strength) {
        strength = s
        generateMnemonic()
    }

    fun generateMnemonic() {
        Bip39Manager.clear(mnemonic)
        mnemonic = Bip39Manager.generate(strength)
        acceptedDisclaimer = false
        goTo(OnboardingStep.DISCLAIMER)
    }

    fun acceptDisclaimerAndShow() {
        if (!acceptedDisclaimer) return
        goTo(OnboardingStep.SHOW_MNEMONIC)
    }

    fun prepareVerification() {
        val m = mnemonic ?: return
        val words = String(m).split(" ")
        verifyPositions = Bip39Manager.pickRandomPositions(words.size)
        verifyOptions = verifyPositions.associateWith { pos ->
            Bip39Manager.generateOptions(words[pos])
        }
        verifyAttempts = 0
        goTo(OnboardingStep.VERIFY)
    }

    fun checkVerification(selected: Map<Int, String>): Boolean {
        val m = mnemonic ?: return false
        val words = String(m).split(" ")
        val allCorrect = verifyPositions.all { pos -> selected[pos] == words[pos] }
        if (allCorrect) {
            goTo(OnboardingStep.SET_PIN)
            return true
        }
        verifyAttempts++
        if (verifyAttempts >= 3) {
            errorMessage = "Too many errors. Go back and write the phrase again."
            goTo(OnboardingStep.SHOW_MNEMONIC)
        } else {
            errorMessage = "Incorrect. Attempt ${verifyAttempts}/3"
        }
        return false
    }

    fun completeSetup(pin: CharArray, panicPin: CharArray): SeedPayload? {
        val m = mnemonic ?: return null
        if (pin.contentEquals(panicPin)) {
            errorMessage = "Panic PIN must differ from the regular PIN"
            return null
        }
        if (CryptoUtils.isWeakPin(String(pin))) {
            errorMessage = "PIN is too weak"
            return null
        }
        val payload = CryptoUtils.encrypt(m, pin)
        val salt = Base64.decode(payload.salt, Base64.NO_WRAP)
        val pinHash = CryptoUtils.hashPin(pin, salt)
        val panicHash = CryptoUtils.hashPin(panicPin, salt)
        val fp = Bip39Manager.fingerprint(m)
        storage.save(payload, pinHash, panicHash, strength.count, fp)
        val result = SeedPayload(mnemonic = m.copyOf(), fingerprint = fp, wordCount = strength.count)
        Bip39Manager.clear(mnemonic)
        mnemonic = null
        pin.fill('\u0000')
        panicPin.fill('\u0000')
        goTo(OnboardingStep.DONE)
        return result
    }

    fun startRestoreFromMnemonic(restored: CharArray) {
        if (!Bip39Manager.validate(restored)) {
            errorMessage = "Invalid seed phrase (checksum error)"
            return
        }
        Bip39Manager.clear(mnemonic)
        mnemonic = restored
        strength = if (restored.count { it == ' ' } + 1 >= 24)
            Bip39Manager.Strength.WORDS_24 else Bip39Manager.Strength.WORDS_12
        goTo(OnboardingStep.SET_PIN)
    }

    override fun onCleared() {
        Bip39Manager.clear(mnemonic)
        mnemonic = null
        super.onCleared()
    }
}
