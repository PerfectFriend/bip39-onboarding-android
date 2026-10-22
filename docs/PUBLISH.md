# Publishing (JitPack)

## Dependency

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// app/build.gradle.kts
dependencies {
    // Preferred (multi-module):
    implementation("com.github.PerfectFriend.bip39-onboarding-android:library:1.2.1")

    // Alternate (root artifact name, if JitPack maps it):
    // implementation("com.github.PerfectFriend:bip39-onboarding-android:1.2.1")
}
```

## Tag a release

```bash
git tag -a 1.2.1 -m "BIP39 Onboarding engine 1.2.1"
git push origin 1.2.1
```

Or GitHub → Releases → Draft a new release → tag `1.2.1`.

Then open: https://jitpack.io/#PerfectFriend/bip39-onboarding-android/1.2.1

Wait for a green build, then use the dependency.

## Local publish

```bash
./gradlew :library:publishToMavenLocal
```

```kotlin
mavenLocal()
implementation("com.github.PerfectFriend:bip39-onboarding:1.2.1")
```
