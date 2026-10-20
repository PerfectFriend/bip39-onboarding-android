<p align="center">
  <img src="assets/banner.svg" alt="BIP39 Secure Onboarding — cyberpunk × Van Gogh × Gaudí" width="100%"/>
</p>

<p align="center">
  <strong>Production-ready BIP39 onboarding for Android</strong><br/>
  Kotlin · Jetpack Compose · Material 3 · Security-first
</p>

<p align="center">
  <a href="#-features"><img src="https://img.shields.io/badge/features-PIN%20%7C%20Panic%20PIN%20%7C%20PDF%2FQR-00f5d4?style=for-the-badge&labelColor=0a0618" alt="Features"/></a>
  <a href="#-security"><img src="https://img.shields.io/badge/security-AES--256--GCM%20%2B%20PBKDF2-ff006e?style=for-the-badge&labelColor=0a0618" alt="Security"/></a>
  <a href="#-quick-start"><img src="https://img.shields.io/badge/stack-Kotlin%20%2B%20Compose-7b2cbf?style=for-the-badge&labelColor=0a0618" alt="Stack"/></a>
  <a href="#license"><img src="https://img.shields.io/badge/license-MIT-ffbe0b?style=for-the-badge&labelColor=0a0618" alt="License"/></a>
</p>

---

## Overview

**BIP39 Secure Onboarding** is a drop-in Android module that implements a complete, security-hardened wallet / key-management onboarding flow according to BIP39 best practices.

Copy it into any project (crypto wallets, password managers, secure messengers, hardware-backed apps) and customize with minimal effort.

### Why this exists

Most onboarding implementations either:

- skip verification quizzes,
- store mnemonics carelessly,
- ignore duress (panic) scenarios,
- or force developers to re-invent the same screens every time.

This pipeline unifies **UX, cryptography, and AI-agent prompts** so you get a consistent, auditable result in minutes — not weeks.

---

## Features

| Area | What you get |
|------|----------------|
| **Create flow** | Splash → Choose → Strength (12/24) → Disclaimer → Show mnemonic → Verify 3 words → PIN → Panic PIN → Encrypt & hand-off |
| **Restore flow** | Paste seed · QR scan · PDF import · checksum validation · same PIN setup |
| **Security** | `CharArray` mnemonics, `SecureRandom`, AES-256-GCM, PBKDF2-HMAC-SHA512 (310k iterations), `FLAG_SECURE` |
| **Duress** | Separate **Panic PIN** that wipes all data and resets the app |
| **Backup** | One-tap copy · offline PDF passport with QR + fingerprint |
| **UX** | Material 3, progress steps, weak-PIN rejection, clear error states |
| **DX** | Ready-made **AI agent prompts** so Cursor / Claude / Grok implement screens consistently |

---

## Architecture

```
┌─────────────────────────────────────────────┐
│                  UI Layer                   │
│  OnboardingNavHost + Screens (Compose)      │
│  SecureScreen (FLAG_SECURE wrapper)         │
└─────────────────────┬───────────────────────┘
                      │
┌─────────────────────▼───────────────────────┐
│               ViewModel Layer               │
│  OnboardingViewModel (state + business)     │
└─────────────────────┬───────────────────────┘
                      │
┌─────────────────────▼───────────────────────┐
│              Domain / Security              │
│  Bip39Manager · CryptoUtils · PinUnlock     │
└─────────────────────┬───────────────────────┘
                      │
┌─────────────────────▼───────────────────────┐
│               Data Layer                    │
│  SecureSeedStorage (EncryptedSharedPrefs)   │
└─────────────────────────────────────────────┘
```

**Flow (state machine)**

```
Splash → Choose (Create | Restore)
  Create: Strength → Disclaimer → Show → Verify → PIN → Panic PIN → Done
  Restore: Paste/QR/PDF → Validate BIP39 → PIN → Panic PIN → Done
```

---

## Security

| Practice | Implementation |
|----------|----------------|
| Entropy | `SecureRandom` only |
| Mnemonic type | `CharArray` (not `String`) to reduce String-pool leakage |
| Checksum | Full BIP39 validation via `cash.z.ecc.android:kotlin-bip39` |
| At-rest encryption | AES-256-GCM; key derived with PBKDF2-HMAC-SHA512 @ 310000 iterations |
| Screenshot block | `WindowManager.LayoutParams.FLAG_SECURE` on sensitive screens |
| Duress | Panic PIN → full wipe of encrypted storage |
| Cleanup | Explicit `fill('\\u0000')` / `fill(0)` after use |
| Weak PINs | Rejected (`000000`, `123456`, repeated digits, etc.) |
| Logging | No seed / PIN in logs |

> **Note:** `EncryptedSharedPreferences` is used for convenience. For greenfield 2026+ projects, migrate to **DataStore + Google Tink** (prompt included in `docs/AI_PROMPTS.md`).

---

## Project layout

```
bip39-onboarding-android/
├── assets/
│   └── banner.svg
├── docs/
│   ├── AI_PROMPTS.md
│   ├── ARCHITECTURE.md
│   ├── INTEGRATION.md
│   ├── QUICK_REFERENCE.md
│   └── SECURITY.md
└── src/main/java/com/example/bip39onboarding/
    ├── data/Models.kt
    ├── security/
    │   ├── Bip39Manager.kt
    │   ├── CryptoUtils.kt
    │   ├── PinUnlock.kt
    │   └── SecureSeedStorage.kt
    └── ui/
        ├── components/
        │   ├── SecureScreen.kt
        │   └── PdfExporter.kt
        └── onboarding/
            ├── OnboardingNavHost.kt
            ├── OnboardingScreens.kt
            └── OnboardingViewModel.kt
```

---

## Quick start

### 1. Dependencies (`app/build.gradle.kts`)

```kotlin
dependencies {
    implementation("cash.z.ecc.android:kotlin-bip39:1.0.9")
    implementation("androidx.security:security-crypto:1.1.0-alpha07")

    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")
}
```

### 2. Manifest

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />

<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### 3. Entry point

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storage = SecureSeedStorage(applicationContext)

        setContent {
            YourTheme {
                if (!storage.isSetup()) {
                    OnboardingNavHost(
                        storage = storage,
                        onComplete = { payload ->
                            // Hand-off: use payload.mnemonic / fingerprint, then clear
                        },
                        onWipe = {
                            val intent = packageManager.getLaunchIntentForPackage(packageName)
                            intent?.addFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                            startActivity(intent)
                            Runtime.getRuntime().exit(0)
                        }
                    )
                } else {
                    // Normal PIN unlock screen
                }
            }
        }
    }
}
```

Full integration notes: **[docs/INTEGRATION.md](docs/INTEGRATION.md)**  
Security checklist: **[docs/SECURITY.md](docs/SECURITY.md)**

---

## AI agent prompts

Stop pasting thousand-line requirements into every chat.

Use the **system prompt + task templates** in:

**[docs/AI_PROMPTS.md](docs/AI_PROMPTS.md)**

Example short task:

```text
Use BIP39 Onboarding Pipeline v1.0.
Implement VerifyMnemonicScreen.
Keep CharArray, FLAG_SECURE, Panic PIN, AES-GCM + 310k PBKDF2.
```

Works with Cursor, Windsurf, Claude, Grok, Devin, and similar agents.

---

## Configuration knobs

```kotlin
object OnboardingConfig {
    val defaultStrength = Bip39Manager.Strength.WORDS_12  // or WORDS_24
    val allowUserChooseStrength = true
    val pinLength = 6                                     // 6 or 8
    val requirePanicPin = true
    val maxVerifyAttempts = 3
    val pbkdf2Iterations = 310_000
    val enablePdfExport = true
}
```

---

## Hand-off payload

After successful create / restore, the host app receives:

```kotlin
data class SeedPayload(
    val mnemonic: CharArray,   // in-memory only — clear when done
    val fingerprint: String,   // short visual check
    val wordCount: Int
)
```

Decrypt / unlock later with `PinUnlock.tryUnlock(pin, storage)`.

---

## Roadmap / extensions

- [ ] BIP39 passphrase (25th word)
- [ ] DataStore + Tink storage migration
- [ ] BiometricPrompt on top of PIN
- [ ] Full CameraX QR restore UI
- [ ] PDF text/QR import parser
- [ ] Multi-language wordlists

---

## License

MIT — free for commercial and open-source use.

---

<p align="center">
  <em>Cyberpunk neon · Van Gogh motion · Gaudí organic structure</em><br/>
  Built for teams that ship secure key UX without reinventing the pipeline every time.
</p>
