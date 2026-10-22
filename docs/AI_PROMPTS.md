# Ready prompts for AI coding agents

Use these prompts to get consistent BIP39 Onboarding implementations without repeating long requirements.

Copy a block into Cursor / Windsurf / Claude / ChatGPT / Grok / Devin / Aider.

---

## 1. System / Project Rule

```text
You are a senior Android developer specializing in secure apps and crypto wallets.

When implementing BIP39 onboarding always follow BIP39 Onboarding Pipeline v1.1:

STACK:
- Kotlin
- Jetpack Compose + Material 3
- Navigation Compose
- cash.z.ecc.android:kotlin-bip39 (CharArray, checksum validation)
- AES-256-GCM + PBKDF2-HMAC-SHA512 (310_000 iterations)
- FLAG_SECURE on all seed screens
- EncryptedSharedPreferences (or DataStore + Tink)

MANDATORY FLOW:
1. Splash
2. Choose: Create New / Restore
3. Strength (12/24 words) — Create only
4. Disclaimer + required responsibility Checkbox
5. Show Mnemonic (grid, copy all, show/hide, anti-screenshot, Export PDF+QR)
6. Verify 3 random words (5 options each)
7. Set PIN (6 digits, confirm, reject weak)
8. Set Panic PIN (must differ; wipe on use)
9. Encrypt & Store → hand-off

RESTORE:
- Paste mnemonic
- Scan QR
- Import PDF
- After checksum validation → same Set PIN + Panic PIN

SECURITY (hard rules):
- CharArray only for mnemonic and PIN
- SecureRandom for entropy
- Clear arrays with fill('\u0000') after use
- Never log seed/PIN
- PDF must include fingerprint + warnings
- Panic PIN wipes all data with no recovery

UI/UX:
- Material 3
- Progress Step X of Y
- Clear errors
- Red screenshot warning on seed screen

CODE:
- Modular (security / ui / data)
- ViewModel for state
- Comments only for security decisions
- Copy-paste ready

If the request is incomplete, complete it using this standard.
```

---

## 2. Full onboarding task

```text
Implement full BIP39 Onboarding Pipeline v1.1 for Android per project standard.

Stack: Kotlin + Jetpack Compose + Material 3 + Navigation Compose.
BIP39 library: cash.z.ecc.android:kotlin-bip39.

Need:
- All flow screens (Create + Restore)
- Bip39Manager, CryptoUtils, SecureSeedStorage
- OnboardingViewModel
- SecureScreen (FLAG_SECURE)
- PDF + QR export
- Panic PIN
- PinUnlockScreen with lockout
- Beautiful Material 3 UI

Deliver complete ready-to-copy files.
```

---

## 3. Single screen example

```text
Per BIP39 Onboarding Pipeline v1.1 implement ShowMnemonicScreen.

Requirements:
- SecureScreen (FLAG_SECURE)
- 3-column grid with word numbers
- Show/Hide toggle
- Copy all button + clipboard auto-clear
- Export PDF + QR button
- Red warning about screenshots
- "I wrote it down" button
- Material 3, clean spacing
```

---

## 4. Restore flow

```text
Per BIP39 Onboarding Pipeline v1.1 implement Restore flow:

1. Three methods: Paste / Scan QR (CameraX + ZXing) / Import PDF
2. Real-time checksum validation
3. On success → Set PIN + Set Panic PIN
4. Clear user-facing errors
5. Production-ready code
```

---

## 5. Storage migration

```text
Per BIP39 Onboarding Pipeline v1.1 migrate SecureSeedStorage from EncryptedSharedPreferences to Jetpack DataStore + Google Tink.

Keep:
- Same API (save / getPayload / wipe / isSetup)
- Seed encryption from PIN (AES-GCM + PBKDF2)
- Master key in Android Keystore
```

---

## 6. Security audit

```text
Security-audit BIP39 Onboarding against Pipeline v1.1 checklist.

Check:
- CharArray usage
- Sensitive data clearing
- FLAG_SECURE
- PBKDF2 strength
- Panic PIN
- No seed in logs
- Checksum validation

Return findings + recommendations.
```

---

## 7. Adapt to project X

```text
Take BIP39 Onboarding Pipeline v1.1 and adapt to our project:

- Package: com.mycompany.wallet
- Theme: dark, primary = #00C853
- PIN length = 8
- Default 24 words
- Add BIP39 passphrase (25th word)

Keep architecture and security practices.
```
