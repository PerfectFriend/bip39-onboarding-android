<p align="center">
  <img src="assets/banner.svg" alt="BIP39 Secure Onboarding" width="100%"/>
</p>

<p align="center">
  <strong>Drop-in BIP39 onboarding engine for Android</strong><br/>
  Create · Restore · PIN · Panic PIN · Passphrase · Biometrics · Tink storage
</p>

<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-cyan.svg?style=for-the-badge&labelColor=0a0618" alt="License"/></a>
  <img src="https://img.shields.io/badge/Android-minSdk%2026-green.svg?style=for-the-badge&labelColor=0a0618" alt="minSdk"/>
  <img src="https://img.shields.io/badge/Kotlin-2.0-purple.svg?style=for-the-badge&labelColor=0a0618" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/version-1.2.1-blue.svg?style=for-the-badge&labelColor=0a0618" alt="version"/>
</p>

---

## Why this exists

Crypto apps keep reinventing the same onboarding: mnemonic generate/restore, verification quiz, PIN/duress PIN, encrypted storage, anti-screenshot hygiene.

This library is a **ready engine**: security core + Compose UI + AI prompts — ship wallets faster without half-baked flows.

---

## Features (v1.2)

| Area | Capability |
|------|------------|
| **Create** | Splash → Create/Restore → 12/24 → Disclaimer → Show seed → 3-word quiz → optional passphrase → PIN → Panic PIN → hand-off |
| **Restore** | Paste · QR · PDF · checksum |
| **Crypto** | BIP39, `CharArray`, AES-256-GCM, PBKDF2-HMAC-SHA512 (310k) |
| **Storage** | `TinkSeedStorage` (preferred) · legacy ESP + migration |
| **Unlock** | PIN + lockout · biometrics · integrity warning |
| **Duress** | Panic PIN wipes all data |
| **UX** | Material 3 · EN/RU · `FLAG_SECURE` · clipboard auto-clear |
| **DX** | Sample app · unit tests · CI · AI prompts |

---

## Project layout

```
bip39-onboarding-android/
├── library/          # Android library (the engine)
├── sample/           # Demo host app
├── docs/             # Integration, security, AI prompts
├── assets/           # Banner
└── .github/workflows # CI
```

---

## Quick start

### Include as a module

```kotlin
// settings.gradle.kts
include(":bip39-onboarding")
project(":bip39-onboarding").projectDir = file("path/to/bip39-onboarding-android/library")

// app/build.gradle.kts
dependencies { implementation(project(":bip39-onboarding")) }
```

### Wire storage + UI

```kotlin
val tink = TinkSeedStorage(context).also { it.migrateFromLegacyIfNeeded() }
val storage: SeedStorage = TinkSeedStorageAdapter(tink)

if (!storage.isSetup()) {
    OnboardingNavHost(storage = storage, onComplete = { payload ->
        val seed = payload.toSeedBytes()
        payload.clear()
    }, onWipe = { /* restart */ })
} else {
    PinUnlockScreen(
        storage = storage,
        onSuccess = { /* enter app */ },
        onPanic = { /* restart */ },
        enableBiometric = true,
        showIntegrityWarning = true
    )
}
```

### Run sample

```bash
./gradlew :sample:assembleDebug
./gradlew :library:testDebugUnitTest
```

---

## Security model (short)

1. Mnemonic in `CharArray` — wipe after use.
2. PIN → PBKDF2 → AES-GCM.
3. Panic PIN → full wipe.
4. Passphrase optional; not stored (flag only).
5. Biometrics gate UX only — do not replace PIN unwrap.
6. `FLAG_SECURE` + clipboard clear 60s.

See [docs/SECURITY.md](docs/SECURITY.md).

---

## Documentation

| Doc | Purpose |
|-----|--------|
| [INTEGRATION.md](docs/INTEGRATION.md) | Dependencies & entry points |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | Layers |
| [SECURITY.md](docs/SECURITY.md) | Threat model |
| [AI_PROMPTS.md](docs/AI_PROMPTS.md) | AI agent prompts |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Contribute |
| [CHANGELOG.md](CHANGELOG.md) | Versions |

---

## License

[MIT](LICENSE)

<p align="center"><em>Understand the keys. Own the flow.</em></p>
