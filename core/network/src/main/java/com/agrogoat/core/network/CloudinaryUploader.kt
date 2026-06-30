package com.agrogoat.core.network

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.agrogoat.core.network.config.CloudinaryConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.MessageDigest

object CloudinaryUploader {
    private val client = OkHttpClient()

    private fun sha1(input: String): String {
        val md = MessageDigest.getInstance("SHA-1")
        val bytes = md.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun uploadImage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return@withContext null
            inputStream.close()

            val mediaType = "image/*".toMediaType()
            val fileBody = bytes.toRequestBody(mediaType)

            // Current timestamp in seconds
            val timestamp = (System.currentTimeMillis() / 1000).toString()

            // Signature generation: timestamp=<timestamp><API_SECRET>
            val signatureString = "timestamp=$timestamp${CloudinaryConfig.API_SECRET}"
            val signature = sha1(signatureString)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "uploaded_goat.jpg", fileBody)
                .addFormDataPart("api_key", CloudinaryConfig.API_KEY)
                .addFormDataPart("timestamp", timestamp)
                .addFormDataPart("signature", signature)
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/${CloudinaryConfig.CLOUD_NAME}/image/upload")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val bodyStr = response.body?.string()
            if (response.isSuccessful) {
                if (bodyStr != null) {
                    val json = JSONObject(bodyStr)
                    return@withContext json.optString("secure_url")
                }
            } else {
                if (bodyStr != null) {
                    try {
                        val json = JSONObject(bodyStr)
                        val errorObj = json.optJSONObject("error")
                        val errMsg = errorObj?.optString("message") ?: "Unknown error"
                        android.util.Log.e("CloudinaryUploader", "Upload failed: $errMsg")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Cloudinary Error: $errMsg", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("CloudinaryUploader", "Upload failed with status code ${response.code}")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
