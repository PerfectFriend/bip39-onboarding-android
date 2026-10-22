package com.example.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bip39onboarding.data.SeedPayload
import com.example.bip39onboarding.security.SecureSeedStorage
import com.example.bip39onboarding.ui.onboarding.OnboardingNavHost
import com.example.bip39onboarding.ui.unlock.PinUnlockScreen

/**
 * Minimal host app demonstrating library integration.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storage = SecureSeedStorage(applicationContext)

        setContent {
            MaterialTheme {
                var unlocked by remember { mutableStateOf<SeedPayload?>(null) }
                var forceOnboarding by remember { mutableStateOf(false) }

                when {
                    forceOnboarding || !storage.isSetup() -> {
                        OnboardingNavHost(
                            storage = storage,
                            onComplete = { payload ->
                                unlocked = payload
                                forceOnboarding = false
                            },
                            onWipe = { restartApp() }
                        )
                    }
                    unlocked == null -> {
                        PinUnlockScreen(
                            storage = storage,
                            onSuccess = { unlocked = it },
                            onPanic = { restartApp() }
                        )
                    }
                    else -> {
                        HomeScreen(
                            fingerprint = unlocked!!.fingerprint,
                            onLock = {
                                unlocked?.clear()
                                unlocked = null
                            }
                        )
                    }
                }
            }
        }
    }

    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        Runtime.getRuntime().exit(0)
    }
}

@Composable
private fun HomeScreen(fingerprint: String, onLock: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Unlocked", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Fingerprint: $fingerprint", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onLock) { Text("Lock") }
    }
}
