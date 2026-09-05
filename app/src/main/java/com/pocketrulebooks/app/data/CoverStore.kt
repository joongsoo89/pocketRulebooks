package com.pocketrulebooks.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

class CoverStore(context: Context) {
    private val appContext = context.applicationContext
    private val dir = File(appContext.filesDir, "covers").apply { mkdirs() }

    fun fileFor(gameId: String): File = File(dir, "$gameId.jpg")

    fun hasCover(gameId: String): Boolean = fileFor(gameId).exists()

    fun saveFromUri(gameId: String, uri: Uri): Boolean {
        return runCatching {
            val original = appContext.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return false
            val scaled = scaleDown(original, 900)
            fileFor(gameId).outputStream().use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            if (scaled !== original) original.recycle()
            true
        }.getOrDefault(false)
    }

    fun delete(gameId: String) {
        fileFor(gameId).delete()
    }

    private fun scaleDown(source: Bitmap, maxEdge: Int): Bitmap {
        val longest = maxOf(source.width, source.height)
        if (longest <= maxEdge) return source
        val ratio = maxEdge.toFloat() / longest
        val width = (source.width * ratio).toInt().coerceAtLeast(1)
        val height = (source.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, width, height, true)
    }
}
