package com.example.bip39onboarding.security

import android.util.Base64
import com.example.bip39onboarding.data.UnlockResult

/**
 * Попытка разблокировки по PIN.
 * Panic PIN → мгновенный wipe.
 */
object PinUnlock {

    fun tryUnlock(pin: CharArray, storage: SecureSeedStorage): UnlockResult {
        if (!storage.isSetup()) return UnlockResult.NotSetup

        val payload = storage.getPayload() ?: return UnlockResult.Error
        val storedPinHash = storage.getPinHash() ?: return UnlockResult.Error
        val storedPanicHash = storage.getPanicPinHash()

        val salt = Base64.decode(payload.salt, Base64.NO_WRAP)
        val inputHash = CryptoUtils.hashPin(pin, salt)

        return when {
            storedPanicHash != null && inputHash == storedPanicHash -> {
                storage.wipe()
                UnlockResult.Panic
            }
            inputHash == storedPinHash -> {
                val mnemonic = CryptoUtils.decrypt(payload, pin)
                if (mnemonic != null && Bip39Manager.validate(mnemonic)) {
                    UnlockResult.Success(mnemonic)
                } else {
                    UnlockResult.Error
                }
            }
            else -> UnlockResult.WrongPin
        }
    }
}
