package com.omar.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeminiProvider(
    private val apiKey: String,
    private val model: String
) : AIProvider {

    override suspend fun ask(prompt: String): String = withContext(Dispatchers.IO) {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("x-goog-api-key", apiKey)
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 30000

            val body = JSONObject().put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", prompt))
                    )
                )
            )
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: throw IllegalStateException("لا يوجد رد من السيرفر (كود $code)")

            if (code !in 200..299) {
                throw IllegalStateException("خطأ $code: $text")
            }

            JSONObject(text)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } finally {
            conn.disconnect()
        }
    }
}
