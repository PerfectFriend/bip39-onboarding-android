package com.example.bip39onboarding.ui.unlock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.bip39onboarding.R
import com.example.bip39onboarding.data.SeedPayload
import com.example.bip39onboarding.data.UnlockResult
import com.example.bip39onboarding.security.Bip39Manager
import com.example.bip39onboarding.security.PinLockout
import com.example.bip39onboarding.security.PinUnlock
import com.example.bip39onboarding.security.SecureSeedStorage
import kotlinx.coroutines.delay

@Composable
fun PinUnlockScreen(
    storage: SecureSeedStorage,
    onSuccess: (SeedPayload) -> Unit,
    onPanic: () -> Unit,
    fingerprintHint: String? = storage.getFingerprint()
) {
    val context = LocalContext.current
    val lockout = remember { PinLockout(context) }

    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var remainingMs by remember { mutableLongStateOf(lockout.remainingLockMs()) }

    LaunchedEffect(remainingMs) {
        while (remainingMs > 0) {
            delay(250)
            remainingMs = lockout.remainingLockMs()
        }
    }

    val locked = remainingMs > 0

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.bip39_unlock_title), style = MaterialTheme.typography.headlineSmall)
        if (!fingerprintHint.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.bip39_fingerprint, fingerprintHint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= 8 && it.all(Char::isDigit)) {
                    pin = it
                    error = null
                }
            },
            label = { Text(stringResource(R.string.bip39_pin_label)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            enabled = !locked,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        if (locked) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.bip39_locked_out, (remainingMs / 1000).toInt().coerceAtLeast(1)),
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (locked || pin.length < 6) return@Button
                val chars = pin.toCharArray()
                when (val result = PinUnlock.tryUnlock(chars, storage)) {
                    is UnlockResult.Success -> {
                        lockout.registerSuccess()
                        val fp = storage.getFingerprint() ?: Bip39Manager.fingerprint(result.mnemonic)
                        val wc = storage.getWordCount()
                        onSuccess(SeedPayload(result.mnemonic, fp, wc))
                        pin = ""
                    }
                    UnlockResult.Panic -> {
                        lockout.registerSuccess()
                        Bip39Manager.clear(chars)
                        pin = ""
                        onPanic()
                    }
                    UnlockResult.WrongPin -> {
                        lockout.registerFailure()
                        remainingMs = lockout.remainingLockMs()
                        error = context.getString(R.string.bip39_wrong_pin)
                        pin = ""
                    }
                    else -> {
                        error = context.getString(R.string.bip39_wrong_pin)
                        pin = ""
                    }
                }
                Bip39Manager.clear(chars)
            },
            enabled = !locked && pin.length >= 6,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.bip39_unlock))
        }
    }
}
