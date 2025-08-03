package com.example.text2ai

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val API_KEY = BuildConfig.GEMINI_API_KEY
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$API_KEY"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    fun askAI(prompt: String, callback: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestBody = gson.toJson(
                    mapOf(
                        "contents" to listOf(
                            mapOf("parts" to listOf(mapOf("text" to prompt)))
                        )
                    )
                )
                val request = Request.Builder()
                    .url(API_URL)
                    .header("Content-Type", "application/json")
                    .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()
                client.newCall(request).enqueue(object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("GeminiService", "Error getting AI response", e)
                        callback("Error: ${e.message}")
                    }
                    override fun onResponse(call: Call, response: Response) {
                        response.body?.string()?.let { json ->
                            Log.e("GeminiService", "API Response: $json")
                            val aiResponse = gson.fromJson(json, GeminiResponse::class.java)
                            val answer = aiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response from AI"
                            callback(answer)
                        }
                    }
                })
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("GeminiService", "Error getting AI response", e)
                    callback("Error: ${e.message}")
                }
            }
        }
    }
}