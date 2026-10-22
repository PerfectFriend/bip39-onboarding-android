package com.example.bip39onboarding.security

import android.content.Context
import android.os.Build
import java.io.File

object DeviceIntegrity {

    data class Report(
        val isRootedHeuristic: Boolean,
        val isEmulatorHeuristic: Boolean,
        val isDebuggable: Boolean,
        val warnings: List<String>
    )

    fun assess(context: Context): Report {
        val rooted = detectRoot()
        val emulator = detectEmulator()
        val debuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val warnings = mutableListOf<String>()
        if (rooted) warnings.add("root_suspected")
        if (emulator) warnings.add("emulator_suspected")
        if (debuggable) warnings.add("debuggable_build")
        return Report(rooted, emulator, debuggable, warnings)
    }

    private fun detectRoot(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"
        )
        if (paths.any { File(it).exists() }) return true
        return try {
            Runtime.getRuntime().exec(arrayOf("which", "su")).inputStream.bufferedReader().readLine() != null
        } catch (_: Exception) {
            false
        }
    }

    private fun detectEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        val brand = Build.BRAND
        val device = Build.DEVICE
        val product = Build.PRODUCT
        return fingerprint.startsWith("generic") || fingerprint.startsWith("unknown") ||
            model.contains("google_sdk") || model.contains("Emulator") ||
            model.contains("Android SDK built for x86") || manufacturer.contains("Genymotion") ||
            (brand.startsWith("generic") && device.startsWith("generic")) ||
            product == "google_sdk" || product.contains("sdk") || product.contains("emulator")
    }
}
