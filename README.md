<p align="center">
  <img src="assets/banner.svg" alt="BIP39 Secure Onboarding" width="100%"/>
</p>

<p align="center">
  <strong>Drop-in BIP39 onboarding engine for Android</strong><br/>
  Create · Restore · PIN · Panic PIN · Passphrase · Biometrics · Tink storage
</p>

<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-cyan.svg?style=for-the-badge&labelColor=0a0618" alt="License"/></a>
  <a href="https://jitpack.io/#PerfectFriend/bip39-onboarding-android"><img src="https://jitpack.io/v/PerfectFriend/bip39-onboarding-android.svg?style=for-the-badge&labelColor=0a0618" alt="JitPack"/></a>
  <img src="https://img.shields.io/badge/Android-minSdk%2026-green.svg?style=for-the-badge&labelColor=0a0618" alt="minSdk"/>
  <img src="https://img.shields.io/badge/version-1.2.1-blue.svg?style=for-the-badge&labelColor=0a0618" alt="version"/>
</p>

---

## Why this exists

Crypto apps keep reinventing the same onboarding. This library is a **ready engine**: security core + Compose UI + AI prompts.

---

## Install (JitPack)

```kotlin
// settings.gradle.kts
maven { url = uri("https://jitpack.io") }

// app/build.gradle.kts
dependencies {
    implementation("com.github.PerfectFriend.bip39-onboarding-android:library:1.2.1")
}
```

1. Create git tag `1.2.1` on GitHub (Releases → New release).
2. Open https://jitpack.io/#PerfectFriend/bip39-onboarding-android/1.2.1 and wait for green build.
3. Sync Gradle.

Full notes: [docs/PUBLISH.md](docs/PUBLISH.md)

---

## Features (v1.2)

| Area | Capability |
|------|------------|
| **Create** | Splash → 12/24 → Disclaimer → Seed → Quiz → Passphrase → PIN → Panic PIN |
| **Restore** | Paste · QR · PDF |
| **Storage** | Tink (preferred) · ESP migration |
| **Unlock** | PIN lockout · biometrics · integrity warning |

---

## Quick start (module)

```kotlin
include(":bip39-onboarding")
project(":bip39-onboarding").projectDir = file("path/to/library")
```

```kotlin
val tink = TinkSeedStorage(context).also { it.migrateFromLegacyIfNeeded() }
val storage: SeedStorage = TinkSeedStorageAdapter(tink)

if (!storage.isSetup()) {
    OnboardingNavHost(storage, onComplete = { it.clear() }, onWipe = { })
} else {
    PinUnlockScreen(storage, onSuccess = { }, onPanic = { })
}
```

```bash
./gradlew :sample:assembleDebug
./gradlew :library:testDebugUnitTest
```

---

## Docs

[INTEGRATION](docs/INTEGRATION.md) · [SECURITY](docs/SECURITY.md) · [AI_PROMPTS](docs/AI_PROMPTS.md) · [PUBLISH](docs/PUBLISH.md) · [ENGINE](docs/ENGINE.md) · [CONTRIBUTING](CONTRIBUTING.md)

## License

[MIT](LICENSE)
