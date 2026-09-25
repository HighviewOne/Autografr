package com.autografr.app.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Re-encodes images as bounded JPEGs so uploads stay under the size caps in
 * storage.rules (profile 5 MB, thumbnail 2 MB).
 */
object ImageScaler {

    /** Downscales already-encoded JPEG bytes so the longest side is at most [maxDimension]. */
    fun scaleBytes(bytes: ByteArray, maxDimension: Int, quality: Int): ByteArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxDimension)
        }
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            ?: throw IOException("Could not decode image")
        return encode(fitWithin(decoded, maxDimension), quality)
    }

    /**
     * Decodes a content:// or file:// image, applies its EXIF rotation (lost when
     * re-encoding), and downscales so the longest side is at most [maxDimension].
     */
    fun scaleUri(context: Context, uri: Uri, maxDimension: Int, quality: Int): ByteArray {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxDimension)
        }
        val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            ?: throw IOException("Could not decode image $uri")
        val rotation = resolver.openInputStream(uri)?.use { exifRotation(it) } ?: 0
        val rotated = if (rotation == 0) decoded else {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        }
        return encode(fitWithin(rotated, maxDimension), quality)
    }

    private fun exifRotation(stream: java.io.InputStream): Int =
        when (ExifInterface(stream).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

    /** Largest power-of-two sample size that keeps both sides at or above [maxDimension]. */
    private fun sampleSizeFor(width: Int, height: Int, maxDimension: Int): Int {
        var sample = 1
        while (width / (sample * 2) >= maxDimension && height / (sample * 2) >= maxDimension) {
            sample *= 2
        }
        return sample
    }

    private fun fitWithin(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val longest = maxOf(bitmap.width, bitmap.height)
        if (longest <= maxDimension) return bitmap
        val scale = maxDimension.toFloat() / longest
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true
        )
    }

    private fun encode(bitmap: Bitmap, quality: Int): ByteArray =
        ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it) }
            .toByteArray()
}
