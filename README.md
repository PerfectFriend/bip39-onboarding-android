# BIP39 Secure Onboarding Pipeline for Android

**Версия:** 1.0.0  
**Дата:** 2026-10-20  
**Стек:** Kotlin + Jetpack Compose + Material 3  
**Назначение:** Готовый, максимально безопасный и красивый онбординг по протоколу BIP39 для любых Android-проектов (криптокошельки, password-менеджеры, secure-приложения).

---

## Что входит в пакет

| Папка / Файл | Описание |
|--------------|----------|
| `docs/` | Полная документация + готовые промпты для AI-агентов |
| `src/main/java/.../security/` | Ядро безопасности (BIP39, шифрование, хранилище) |
| `src/main/java/.../ui/onboarding/` | Все экраны + навигация + ViewModel |
| `src/main/java/.../ui/components/` | Переиспользуемые Compose-компоненты |
| `src/main/java/.../data/` | Модели и результаты |
| `docs/AI_PROMPTS.md` | **Готовые промпты** для Cursor / Windsurf / Claude / Grok / Devin |

---

## Быстрый старт (5 минут)

1. Скопируйте модуль `bip39onboarding` в свой проект.
2. Добавьте зависимости из `docs/INTEGRATION.md`.
3. В `MainActivity` / корневом `NavHost` вызовите:

```kotlin
if (!SecureSeedStorage(context).isSetup()) {
    OnboardingNavHost(
        onComplete = { /* перейти в основное приложение */ },
        onWipe = { /* restart app */ }
    )
} else {
    // обычный вход по PIN
}
```

4. Готово.

---

## Ключевые возможности

- Генерация 12 / 24 слов (BIP39, CharArray, SecureRandom)
- Disclaimer + обязательный чекбокс ответственности
- Запрет скриншотов (`FLAG_SECURE`) на критических экранах
- Копирование всей фразы одним касанием
- Экспорт PDF + QR-код (offline backup)
- Проверка 3 случайных слов (quiz из 5 вариантов)
- Обычный PIN + Panic PIN (принуждение → полный wipe)
- Шифрование AES-256-GCM + PBKDF2-HMAC-SHA512 (310k итераций)
- Restore: вставка / QR / PDF
- Полная очистка CharArray после использования
- Fingerprint seed для визуальной проверки

---

## Документация

- [Интеграция](docs/INTEGRATION.md)
- [Архитектура и flow](docs/ARCHITECTURE.md)
- [Чеклист безопасности](docs/SECURITY.md)
- **[Готовые промпты для AI-агентов](docs/AI_PROMPTS.md)** ← самое важное

---

## Рекомендации по использованию AI-агентов

Вместо того чтобы каждый раз писать огромный промпт — используйте готовые шаблоны из `docs/AI_PROMPTS.md`.

Пример:

```
Используй стандартный BIP39 Onboarding Pipeline v1.0 для Android.
Стек: Kotlin + Compose + Material3.
Сделай экран VerifyMnemonic по спецификации.
```

Агент получит консистентный, безопасный и красивый результат.

---

## Лицензия

MIT. Можно использовать в коммерческих проектах без ограничений.
