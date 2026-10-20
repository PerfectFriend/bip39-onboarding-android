# Quick Reference — BIP39 Onboarding v1.0

## Минимальный набор файлов для копирования

```
security/
  Bip39Manager.kt
  CryptoUtils.kt
  SecureSeedStorage.kt
  PinUnlock.kt

data/
  Models.kt

ui/
  components/
    SecureScreen.kt
    PdfExporter.kt
  onboarding/
    OnboardingViewModel.kt
    OnboardingScreens.kt
    OnboardingNavHost.kt
```

## Одна строка для входа

```kotlin
OnboardingNavHost(
    onComplete = { payload -> /* передать в AppViewModel */ },
    onWipe = { /* restart */ }
)
```

## Готовый промпт (копируй как есть)

```
Используй BIP39 Onboarding Pipeline v1.0 (стандарт проекта).
Сделай [нужный экран / доработку].
Сохрани CharArray, FLAG_SECURE, Panic PIN, AES-GCM + 310k PBKDF2.
```

## Чеклист перед релизом

- [ ] FLAG_SECURE работает (скриншот чёрный)
- [ ] Panic PIN стирает данные
- [ ] Checksum валидируется
- [ ] Нет логов seed
- [ ] PDF содержит fingerprint + предупреждения
- [ ] Слабые PIN отклоняются
- [ ] CharArray очищаются
