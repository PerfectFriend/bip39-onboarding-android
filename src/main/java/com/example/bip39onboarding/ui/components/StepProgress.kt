package com.example.bip39onboarding.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bip39onboarding.R
import com.example.bip39onboarding.data.OnboardingStep

@Composable
fun StepProgress(step: OnboardingStep) {
    val (current, total) = step.toProgress()
    if (total <= 0) return
    Text(
        text = stringResource(R.string.bip39_step_progress, current, total),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    LinearProgressIndicator(
        progress = { current.toFloat() / total },
        modifier = Modifier.fillMaxWidth()
    )
}

private fun OnboardingStep.toProgress(): Pair<Int, Int> = when (this) {
    OnboardingStep.SPLASH -> 0 to 0
    OnboardingStep.CHOOSE -> 1 to 8
    OnboardingStep.STRENGTH -> 2 to 8
    OnboardingStep.DISCLAIMER -> 3 to 8
    OnboardingStep.SHOW_MNEMONIC -> 4 to 8
    OnboardingStep.VERIFY -> 5 to 8
    OnboardingStep.SET_PIN -> 6 to 8
    OnboardingStep.SET_PANIC_PIN -> 7 to 8
    OnboardingStep.RESTORE_CHOOSE,
    OnboardingStep.RESTORE_PASTE,
    OnboardingStep.RESTORE_QR,
    OnboardingStep.RESTORE_PDF -> 2 to 5
    OnboardingStep.DONE -> 8 to 8
}
