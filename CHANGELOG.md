# Changelog

## 1.1.0 — 2026-10-22

### Added
- Full restore: paste / QR (CameraX + ZXing) / PDF import
- `PinUnlockScreen` with exponential lockout
- `PinLockout` (5 failures → backoff, max 5 min)
- Clipboard auto-clear after 60s
- Step progress indicator
- EN + RU string resources
- Android library `build.gradle.kts`, manifest, backup exclude rules
- Unit tests for BIP39 + crypto
- Sample host `MainActivity`
- LICENSE (MIT), `.gitignore`, `consumer-rules.pro`, CI workflow
- `PdfSeedExtractor` for offline PDF/text backups

### Security
- `allowBackup=false` + explicit exclude of `secure_seed_prefs`
- Data extraction rules for cloud/device transfer

## 1.0.0 — 2026-10-20

- Initial create flow, security core, docs, English README + banner
