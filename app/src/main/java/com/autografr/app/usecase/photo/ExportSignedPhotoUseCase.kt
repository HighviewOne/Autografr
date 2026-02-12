package com.autografr.app.usecase.photo

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.autografr.app.domain.util.Result
import java.io.OutputStream
import javax.inject.Inject

class ExportSignedPhotoUseCase @Inject constructor(
    private val context: Context
) {
    operator fun invoke(bitmap: Bitmap, fileName: String): Result<String> {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Autografr")
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return Result.error("Failed to create media entry")

            val outputStream: OutputStream = resolver.openOutputStream(uri)
                ?: return Result.error("Failed to open output stream")

            outputStream.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
            }

            Result.success(uri.toString())
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to export photo", e)
        }
    }
}
