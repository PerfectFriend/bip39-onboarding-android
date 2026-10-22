# Changelog

## 1.2.1 — 2026-10-22

### Open-source engine polish
- Multi-module layout: `:library` + `:sample`
- Gradle Wrapper (8.9) + root/settings for real CI
- README rewritten for engine positioning (v1.2 features)
- CONTRIBUTING.md
- CI runs `:library:testDebugUnitTest` and `:sample:assembleDebug`

## 1.2.0 — 2026-10-22

### Added
- BIP39 optional passphrase (25th word)
- BiometricHelper + biometric button on unlock screen
- TinkSeedStorage (DataStore + Tink) + ESP migration
- SeedStorage interface (ESP / Tink adapters)
- DeviceIntegrity root/emulator/debug warning

### Notes
- Biometrics do not replace PIN-derived encryption
- Prefer TinkSeedStorage for greenfield projects

## 1.1.0 — 2026-10-22

- Full restore, PinUnlock, lockout, tests, sample, CI, MIT

## 1.0.0 — 2026-10-20

- Initial create flow, security core, docs, README + banner
