# Архитектура BIP39 Onboarding Pipeline

## Общий flow (State Machine)

```
┌─────────────┐
│   Splash    │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│ Choose: Create/Restore │
└──────┬──────────┬───┘
       │          │
   Create      Restore
       │          │
       ▼          ▼
┌──────────┐  ┌──────────────────────┐
│ Strength │  │ Input: Paste / QR / PDF │
│ 12/24    │  └──────────┬───────────┘
└────┬─────┘             │
     ▼                   ▼
┌────────────┐     ┌─────────────┐
│ Disclaimer │     │ Validate BIP39 │
│ + Checkbox │     └──────┬──────┘
└────┬───────┘            │
     ▼                    │
┌────────────────┐        │
│ Show Mnemonic  │        │
│ Copy / PDF / QR│        │
└────┬───────────┘        │
     ▼                    │
┌────────────────┐        │
│ Verify 3 words │        │
└────┬───────────┘        │
     ▼                    │
┌────────────────┐        │
│   Set PIN      │◄───────┘
└────┬───────────┘
     ▼
┌────────────────┐
│ Set Panic PIN  │
└────┬───────────┘
     ▼
┌────────────────┐
│ Encrypt & Save │
└────┬───────────┘
     ▼
┌────────────────┐
│  Hand-off /    │
│  Enter App     │
└────────────────┘
```

## Слои

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
│  Bip39Manager · CryptoUtils · PinManager    │
└─────────────────────┬───────────────────────┘
                      │
┌─────────────────────▼───────────────────────┐
│               Data Layer                    │
│  SecureSeedStorage (EncryptedSharedPrefs)   │
└─────────────────────────────────────────────┘
```

## Ключевые решения безопасности

| Решение | Почему |
|---------|--------|
| `CharArray` вместо `String` | Меньше шансов попасть в String pool и логи |
| `SecureRandom` | Криптографически стойкая энтропия |
| PBKDF2 310k итераций | Защита от brute-force PIN |
| AES-256-GCM | Authenticated encryption |
| `FLAG_SECURE` | Запрет скриншотов и recents preview |
| Panic PIN | Duress protection (стандарт в secure-приложениях) |
| Очистка массивов | `fill('\u0000')` после использования |
| Fingerprint | Пользователь может проверить правильность restore |

## Передача ключа в основное приложение

После успешного онбординга / unlock приложение получает:

```kotlin
data class SeedPayload(
    val mnemonic: CharArray,          // только в памяти, не сохранять!
    val seedBytes: ByteArray,         // BIP39 seed (опционально)
    val fingerprint: String,          // для UI
    val wordCount: Int
)
```

**Рекомендация:** сразу после получения зашифруйте seed ещё раз ключом из Android Keystore (если нужна дополнительная защита) или держите только в ViewModel с `onCleared()`.

## Расширение

- Добавить поддержку passphrase (25-е слово)
- Добавить Shamir Secret Sharing (опционально)
- Биометрия поверх PIN (BiometricPrompt)
- Multi-language wordlists (сейчас English)
