package com.autografr.app.usecase.photo

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import javax.inject.Inject

class CapturePhotoUseCase @Inject constructor(
    private val context: Context
) {
    fun createTempPhotoUri(): Uri {
        val photoFile = File.createTempFile(
            "autografr_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }
}
