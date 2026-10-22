package com.example.bip39onboarding.ui.restore

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bip39onboarding.R
import com.example.bip39onboarding.security.Bip39Manager
import com.example.bip39onboarding.ui.components.SecureScreen
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

@Composable
fun RestoreChooseScreen(
    onPaste: () -> Unit,
    onQr: () -> Unit,
    onPdf: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.bip39_restore_choose_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onPaste, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.bip39_restore_paste))
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onQr, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.bip39_restore_qr))
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onPdf, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.bip39_restore_pdf))
        }
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.bip39_back))
        }
    }
}

@Composable
fun RestorePasteScreen(
    onValidMnemonic: (CharArray) -> Unit,
    onBack: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())
    ) {
        Text(stringResource(R.string.bip39_restore_paste_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it; error = null },
            modifier = Modifier.fillMaxWidth().height(160.dp),
            placeholder = { Text("word1 word2 word3 ...") }
        )
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val cleaned = text.trim().lowercase().replace(Regex("\\s+"), " ")
                val chars = cleaned.toCharArray()
                if (Bip39Manager.validate(chars)) {
                    onValidMnemonic(chars)
                } else {
                    error = context.getString(R.string.bip39_restore_invalid)
                    Bip39Manager.clear(chars)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.bip39_continue)) }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.bip39_back))
        }
    }
}

@Composable
fun RestoreQrScreen(
    onValidMnemonic: (CharArray) -> Unit,
    onBack: () -> Unit
) {
    var error by remember { mutableStateOf<String?>(null) }
    var handled by remember { mutableStateOf(false) }

    SecureScreen {
        Column(Modifier.fillMaxSize()) {
            Text(
                stringResource(R.string.bip39_qr_hint),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
            }
            AndroidView(
                factory = { ctx ->
                    DecoratedBarcodeView(ctx).apply {
                        barcodeView.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
                        initializeFromIntent((ctx as? Activity)?.intent ?: Intent())
                        decodeContinuous(object : BarcodeCallback {
                            override fun barcodeResult(result: BarcodeResult?) {
                                if (handled || result == null) return
                                val raw = result.text?.trim()?.lowercase()?.replace(Regex("\\s+"), " ") ?: return
                                val chars = raw.toCharArray()
                                if (Bip39Manager.validate(chars)) {
                                    handled = true
                                    pause()
                                    onValidMnemonic(chars)
                                } else {
                                    Bip39Manager.clear(chars)
                                    error = ctx.getString(R.string.bip39_restore_invalid)
                                }
                            }
                            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
                        })
                        resume()
                    }
                },
                modifier = Modifier.fillMaxWidth().weight(1f),
                update = { it.resume() }
            )
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(stringResource(R.string.bip39_back))
            }
        }
    }
}

@Composable
fun RestorePdfScreen(
    onValidMnemonic: (CharArray) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        fileName = queryDisplayName(context, uri)
        val extracted = PdfSeedExtractor.extract(context, uri)
        if (extracted != null && Bip39Manager.validate(extracted)) {
            onValidMnemonic(extracted)
        } else {
            Bip39Manager.clear(extracted)
            error = context.getString(R.string.bip39_pdf_fail)
        }
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.bip39_restore_pdf), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        if (fileName != null) {
            Text(fileName!!, style = MaterialTheme.typography.bodySmall)
        }
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                error = null
                launcher.launch(arrayOf("application/pdf", "text/plain", "*/*"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.bip39_pdf_pick))
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.bip39_back))
        }
    }
}

private fun queryDisplayName(context: android.content.Context, uri: Uri): String? {
    context.contentResolver.query(uri, null, null, null, null)?.use { c ->
        val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx >= 0 && c.moveToFirst()) return c.getString(idx)
    }
    return uri.lastPathSegment
}

object PdfSeedExtractor {
    private val WORD_LINE = Pattern.compile(
        "(?:\\b[a-z]+\\b[\\s,;]+){11,23}\\b[a-z]+\\b",
        Pattern.CASE_INSENSITIVE
    )

    fun extract(context: android.content.Context, uri: Uri): CharArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val text = BufferedReader(InputStreamReader(input)).readText().lowercase()
                val prefixed = Regex("mnemonic\\s*[:=]\\s*([a-z]+(?:\\s+[a-z]+){11,23})")
                    .find(text)?.groupValues?.getOrNull(1)
                if (prefixed != null) {
                    val chars = prefixed.trim().replace(Regex("\\s+"), " ").toCharArray()
                    if (Bip39Manager.validate(chars)) return chars
                    Bip39Manager.clear(chars)
                }
                val matcher = WORD_LINE.matcher(text)
                while (matcher.find()) {
                    val candidate = matcher.group().trim()
                        .replace(Regex("[,;]+"), " ")
                        .replace(Regex("\\s+"), " ")
                    val words = candidate.split(" ")
                    if (words.size == 12 || words.size == 24) {
                        val chars = candidate.toCharArray()
                        if (Bip39Manager.validate(chars)) return chars
                        Bip39Manager.clear(chars)
                    }
                }
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
