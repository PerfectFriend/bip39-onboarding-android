package com.example.bip39onboarding.security

import android.util.Base64
import com.example.bip39onboarding.data.UnlockResult

object PinUnlock {

    fun tryUnlock(pin: CharArray, storage: SecureSeedStorage): UnlockResult {
        return tryUnlock(pin, EspSeedStorageAdapter(storage))
    }

    fun tryUnlock(pin: CharArray, storage: SeedStorage): UnlockResult {
        if (!storage.isSetup()) return UnlockResult.NotSetup
        val payload = storage.getPayload() ?: return UnlockResult.Error
        val salt = try {
            Base64.decode(payload.salt, Base64.NO_WRAP)
        } catch (_: Exception) {
            return UnlockResult.Error
        }
        val pinHash = CryptoUtils.hashPin(pin, salt)
        val panicHash = storage.getPanicPinHash()
        val storedPinHash = storage.getPinHash()
        if (panicHash != null && pinHash == panicHash) {
            storage.wipe()
            return UnlockResult.Panic
        }
        if (storedPinHash == null || pinHash != storedPinHash) {
            return UnlockResult.WrongPin
        }
        val mnemonic = CryptoUtils.decrypt(payload, pin) ?: return UnlockResult.Error
        return UnlockResult.Success(mnemonic)
    }
}
