# Changelog

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

### Added
- Full restore: paste / QR / PDF import
- PinUnlockScreen with exponential lockout
- Clipboard auto-clear, step progress, EN/RU strings
- Library module, unit tests, sample host, CI, MIT LICENSE

## 1.0.0 — 2026-10-20

- Initial create flow, security core, docs, English README + banner
