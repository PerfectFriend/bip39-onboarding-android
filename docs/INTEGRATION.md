# BIP39 Onboarding Integration

## 1. Dependencies (app/build.gradle.kts)

```kotlin
dependencies {
    implementation("cash.z.ecc.android:kotlin-bip39:1.0.9")
    implementation("androidx.security:security-crypto:1.1.0-alpha07")

    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
```

> Note: EncryptedSharedPreferences is deprecated but still works. For new projects prefer DataStore + Google Tink.

## 2. AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />

<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.bip39.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

Also set `android:allowBackup="false"` and reference `@xml/backup_rules` / `@xml/data_extraction_rules`.

## 3. ProGuard / R8

Use `consumer-rules.pro` shipped with the library.

## 4. Entry point

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storage = SecureSeedStorage(applicationContext)

        setContent {
            YourAppTheme {
                if (!storage.isSetup()) {
                    OnboardingNavHost(
                        storage = storage,
                        onComplete = { payload -> /* hand-off */ },
                        onWipe = { /* restart after panic */ }
                    )
                } else {
                    PinUnlockScreen(
                        storage = storage,
                        onSuccess = { payload -> /* enter app */ },
                        onPanic = { /* restart */ }
                    )
                }
            }
        }
    }
}
```

## 5. Config

```kotlin
object OnboardingConfig {
    val defaultStrength = Bip39Manager.Strength.WORDS_12
    val allowUserChooseStrength = true
    val pinLength = 6
    val requirePanicPin = true
    val maxVerifyAttempts = 3
    val pbkdf2Iterations = 310_000
    val enablePdfExport = true
}
```

## 6. Testing

- Unit: `Bip39Manager.validate()`, `CryptoUtils.encrypt/decrypt`
- UI: quiz, FLAG_SECURE (manual)
- Panic PIN wipes prefs
