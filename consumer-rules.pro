# BIP39 Onboarding — consumer ProGuard / R8 rules

-keep class cash.z.ecc.android.bip39.** { *; }
-keep class com.example.bip39onboarding.security.** { *; }
-keep class com.example.bip39onboarding.data.** { *; }

-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**
