# Contributing

Thanks for helping make BIP39 onboarding safer and easier to ship.

## Principles

1. **Security first** — never log mnemonics/PINs; prefer `CharArray`; clear secrets after use.
2. **API stability** — treat `SeedStorage`, `SeedPayload`, `OnboardingNavHost`, `PinUnlockScreen` as public surface.
3. **Compose + Material 3** for UI changes.
4. **Tests** for crypto/BIP39 changes (`library` unit tests).

## Setup

```bash
./gradlew :library:testDebugUnitTest
./gradlew :sample:assembleDebug
```

Requires JDK 17 and Android SDK 35.

## Pull requests

- One concern per PR when possible.
- Update `CHANGELOG.md` under a new version section if behavior changes.
- Keep AI prompts in `docs/AI_PROMPTS.md` aligned with real code.

## Security reports

If you find a vulnerability, open a **private** security advisory on GitHub or contact the maintainers — do not open a public issue with exploit details.
