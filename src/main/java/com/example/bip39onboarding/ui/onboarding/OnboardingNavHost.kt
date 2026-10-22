package com.example.bip39onboarding.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bip39onboarding.data.OnboardingStep
import com.example.bip39onboarding.data.SeedPayload
import com.example.bip39onboarding.security.Bip39Manager
import com.example.bip39onboarding.security.SecureSeedStorage
import com.example.bip39onboarding.ui.components.StepProgress
import com.example.bip39onboarding.ui.restore.RestoreChooseScreen
import com.example.bip39onboarding.ui.restore.RestorePasteScreen
import com.example.bip39onboarding.ui.restore.RestorePdfScreen
import com.example.bip39onboarding.ui.restore.RestoreQrScreen

@Composable
fun OnboardingNavHost(
    storage: SecureSeedStorage = SecureSeedStorage(LocalContext.current),
    onComplete: (SeedPayload) -> Unit,
    onWipe: () -> Unit = {}
) {
    val context = LocalContext.current
    val vm: OnboardingViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return OnboardingViewModel(storage) as T
            }
        }
    )

    var tempPin by remember { mutableStateOf<CharArray?>(null) }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.goTo(OnboardingStep.RESTORE_QR)
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp)) {
        StepProgress(vm.step)

        when (vm.step) {
            OnboardingStep.SPLASH -> SplashScreen(onContinue = { vm.goTo(OnboardingStep.CHOOSE) })

            OnboardingStep.CHOOSE -> ChooseScreen(
                onCreate = vm::chooseCreate,
                onRestore = vm::chooseRestore
            )

            OnboardingStep.STRENGTH -> StrengthScreen(onSelect = vm::setStrength)

            OnboardingStep.DISCLAIMER -> DisclaimerScreen(
                accepted = vm.acceptedDisclaimer,
                onAcceptedChange = { vm.acceptedDisclaimer = it },
                onContinue = vm::acceptDisclaimerAndShow
            )

            OnboardingStep.SHOW_MNEMONIC -> {
                val m = vm.mnemonic
                if (m != null) {
                    ShowMnemonicScreen(
                        mnemonic = m,
                        fingerprint = Bip39Manager.fingerprint(m),
                        onContinue = vm::prepareVerification
                    )
                }
            }

            OnboardingStep.VERIFY -> VerifyMnemonicScreen(
                positions = vm.verifyPositions,
                options = vm.verifyOptions,
                error = vm.errorMessage,
                onCheck = { selected -> vm.checkVerification(selected) }
            )

            OnboardingStep.SET_PIN -> SetPinScreen(
                titleRes = true,
                isPanic = false,
                errorMessage = vm.errorMessage,
                onComplete = { pin ->
                    tempPin = pin
                    vm.goTo(OnboardingStep.SET_PANIC_PIN)
                }
            )

            OnboardingStep.SET_PANIC_PIN -> SetPinScreen(
                titleRes = false,
                isPanic = true,
                errorMessage = vm.errorMessage,
                onComplete = { panic ->
                    val pin = tempPin
                    if (pin != null) {
                        val payload = vm.completeSetup(pin, panic)
                        Bip39Manager.clear(tempPin)
                        tempPin = null
                        if (payload != null) onComplete(payload)
                    }
                }
            )

            OnboardingStep.RESTORE_CHOOSE -> RestoreChooseScreen(
                onPaste = { vm.goTo(OnboardingStep.RESTORE_PASTE) },
                onQr = {
                    val ok = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (ok) vm.goTo(OnboardingStep.RESTORE_QR)
                    else cameraPermission.launch(Manifest.permission.CAMERA)
                },
                onPdf = { vm.goTo(OnboardingStep.RESTORE_PDF) },
                onBack = { vm.goTo(OnboardingStep.CHOOSE) }
            )

            OnboardingStep.RESTORE_PASTE -> RestorePasteScreen(
                onValidMnemonic = { chars -> vm.startRestoreFromMnemonic(chars) },
                onBack = { vm.goTo(OnboardingStep.RESTORE_CHOOSE) }
            )

            OnboardingStep.RESTORE_QR -> RestoreQrScreen(
                onValidMnemonic = { chars -> vm.startRestoreFromMnemonic(chars) },
                onBack = { vm.goTo(OnboardingStep.RESTORE_CHOOSE) }
            )

            OnboardingStep.RESTORE_PDF -> RestorePdfScreen(
                onValidMnemonic = { chars -> vm.startRestoreFromMnemonic(chars) },
                onBack = { vm.goTo(OnboardingStep.RESTORE_CHOOSE) }
            )

            OnboardingStep.DONE -> { }
        }
    }
}
