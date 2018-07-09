package com.iwallic.app.utils

import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.awt.font.TextAttribute.WIDTH
import com.google.zxing.EncodeHintType
import android.R.attr.level
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.*


object QRCodeUtils {
    fun Generate(content: String, size: Int = 300): Bitmap? {
        val result: BitMatrix
        try {
            result = MultiFormatWriter().encode(content,
                    BarcodeFormat.QR_CODE, size, size, Hashtable<EncodeHintType, Any>().apply {
                put(EncodeHintType.CHARACTER_SET, "utf-8") //编码
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H) //容错率
                put(EncodeHintType.MARGIN, -1)
            })
        } catch (iae: IllegalArgumentException) {
            // Unsupported format
            return null
        }

        val w = result.getWidth()
        val h = result.getHeight()
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (result.get(x, y)) BLACK else WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, size, 0, 0, w, h)
        return bitmap
    }
}
