package com.example.storage

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MediaStoreExporter {

    enum class ExportFormat(val displayName: String, val extension: String, val mimeType: String) {
        JPEG("JPEG", ".jpg", "image/jpeg"),
        PNG("PNG", ".png", "image/png")
    }

    /**
     * Saves a graded photograph to MediaStore in Pictures/AmanColorLab.
     */
    suspend fun exportPhoto(
        context: Context,
        bitmap: Bitmap,
        format: ExportFormat,
        quality: Int = 95
    ): Result<Uri> = withContext(Dispatchers.IO) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "AmanColorLab_$timeStamp${format.extension}"
        val albumDir = "Pictures/AmanColorLab"

        val compressFormat = if (format == ExportFormat.PNG) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }
        val clampedQuality = quality.coerceIn(50, 100)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, albumDir)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext Result.failure(IllegalStateException("Could not create MediaStore entry."))

                resolver.openOutputStream(imageUri)?.use { outStream ->
                    val success = bitmap.compress(compressFormat, clampedQuality, outStream)
                    if (!success) {
                        resolver.delete(imageUri, null, null)
                        return@withContext Result.failure(IllegalStateException("Failed to compress photograph."))
                    }
                } ?: run {
                    resolver.delete(imageUri, null, null)
                    return@withContext Result.failure(IllegalStateException("Unable to open output stream."))
                }

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)

                Result.success(imageUri)
            } else {
                // Legacy storage for API 24-28
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val targetDir = File(picturesDir, "AmanColorLab")
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }
                val destFile = File(targetDir, fileName)
                FileOutputStream(destFile).use { out ->
                    bitmap.compress(compressFormat, clampedQuality, out)
                }

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, destFile.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, format.mimeType)
                    put(MediaStore.Images.Media.TITLE, fileName)
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: Uri.fromFile(destFile)

                Result.success(uri)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
