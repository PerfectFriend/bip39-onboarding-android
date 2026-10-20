package com.example.bip39onboarding.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

/**
 * Генерация offline PDF-паспорта с seed-фразой и QR-кодом.
 */
object PdfExporter {

    fun export(context: Context, mnemonic: CharArray, fingerprint: String) {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }
        val bodyPaint = Paint().apply {
            textSize = 12f
            color = Color.BLACK
        }
        val warnPaint = Paint().apply {
            textSize = 11f
            color = Color.RED
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Header
        canvas.drawText("OFFLINE BACKUP — KEEP SECRET", 40f, 50f, titlePaint)
        canvas.drawText("Fingerprint: $fingerprint", 40f, 80f, bodyPaint)
        canvas.drawText("Created: ${LocalDate.now()}", 40f, 100f, bodyPaint)

        // QR
        val size = 220
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(String(mnemonic), BarcodeFormat.QR_CODE, size, size)
        val qrBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                qrBitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        canvas.drawBitmap(qrBitmap, 187f, 130f, null)

        // Words
        val words = String(mnemonic).split(" ")
        words.forEachIndexed { i, word ->
            val col = i % 3
            val row = i / 3
            val x = 40f + col * 180f
            val y = 390f + row * 28f
            canvas.drawText("${i + 1}. $word", x, y, bodyPaint)
        }

        // Warning
        canvas.drawText("НИКОГДА не фотографируйте и не отправляйте этот файл!", 40f, 780f, warnPaint)
        canvas.drawText("Храните только offline. Потеря = потеря доступа навсегда.", 40f, 800f, warnPaint)

        pdf.finishPage(page)

        val fileName = "seed_backup_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Сохранить PDF-паспорт"))
    }
}
