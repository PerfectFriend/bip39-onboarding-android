package com.example.bip39onboarding.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.bip39onboarding.R

@Composable
fun PassphraseScreen(
    onSkip: () -> Unit,
    onConfirm: (CharArray) -> Unit
) {
    var phrase by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.bip39_passphrase_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.bip39_passphrase_body), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = phrase,
            onValueChange = { phrase = it; error = null },
            label = { Text(stringResource(R.string.bip39_passphrase_label)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it; error = null },
            label = { Text(stringResource(R.string.bip39_passphrase_confirm)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                when {
                    phrase.isEmpty() -> onSkip()
                    phrase != confirm -> error = "Passphrases do not match"
                    else -> onConfirm(phrase.toCharArray())
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.bip39_continue)) }
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.bip39_passphrase_skip))
        }
    }
}
