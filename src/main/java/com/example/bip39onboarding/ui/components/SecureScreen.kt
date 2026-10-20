package com.example.bip39onboarding.ui.components

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Обёртка, включающая FLAG_SECURE на время жизни composable.
 * Блокирует скриншоты, screen recording и recents preview.
 */
@Composable
fun SecureScreen(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    content()
}
