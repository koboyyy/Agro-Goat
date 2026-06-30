package com.agrogoat.core.designsystem.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest

object ImageCache {
    private val httpClient = OkHttpClient()
    // Memory Cache: 1/8 of available VM memory
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    // Helper to generate a safe unique filename based on the URL MD5 hash
    private fun md5(url: String): String {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(url.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Get image from Memory Cache
    fun getFromMemory(url: String): Bitmap? {
        return memoryCache.get(url)
    }

    // Get image from Disk Cache
    suspend fun getFromDisk(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val key = md5(url)
            val cacheFile = File(context.cacheDir, key)
            if (cacheFile.exists()) {
                val bytes = cacheFile.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    // Put back to memory cache
                    memoryCache.put(url, bitmap)
                    return@withContext bitmap
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    // Save image bytes to Disk Cache and memory cache
    suspend fun saveToCache(context: Context, url: String, bytes: ByteArray): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                // Save to memory cache
                memoryCache.put(url, bitmap)

                // Save to disk cache
                val key = md5(url)
                val cacheFile = File(context.cacheDir, key)
                cacheFile.writeBytes(bytes)
                return@withContext bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    // Fetch from memory/disk or download and cache it
    suspend fun fetchAndCacheImage(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
        // 1. Check memory cache
        val memoryBitmap = getFromMemory(url)
        if (memoryBitmap != null) {
            return@withContext memoryBitmap
        }

        // 2. Check disk cache
        val diskBitmap = getFromDisk(context, url)
        if (diskBitmap != null) {
            return@withContext diskBitmap
        }

        // 3. Download from network
        try {
            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes()
                    if (bytes != null) {
                        return@withContext saveToCache(context, url, bytes)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
