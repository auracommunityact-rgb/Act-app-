package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

object AuraGeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun askGeminiCopilot(prompt: String, contextHistory: String = ""): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder")) {
            return@withContext getOfflineAuraAIResponse(prompt)
        }

        // Clean user prompt to avoid breaking manual JSON structure
        val escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n")
        val escapedHistory = contextHistory.replace("\"", "\\\"").replace("\n", "\\n")

        val jsonRequest = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "System: You are Aura AI, the cyber-companion of Aura Community (ACT). Keep answers concise, helpful and gaming-focused.\n\nHistory: $escapedHistory\n\nPrompt: $escapedPrompt"
                    }
                  ]
                }
              ],
              "generationConfig": {
                "temperature": 0.7,
                "maxOutputTokens": 800
              }
            }
        """.trimIndent()

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val body = jsonRequest.toRequestBody("application/json".toMediaType())
        val okRequest = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        try {
            client.newCall(okRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Signal error. Please verify your internet connection or try again shortly."
                }
                val bodyString = response.body?.string() ?: ""
                parseGeminiTextResponse(bodyString)
            }
        } catch (e: IOException) {
            "Network timeout. The Aura server is currently busy. Try again!"
        } catch (e: Exception) {
            getOfflineAuraAIResponse(prompt)
        }
    }

    private fun parseGeminiTextResponse(rawJson: String): String {
        return try {
            // High reliability simplified manual finder to avoid dependency misalignment
            val searchKey = "\"text\":"
            val index = rawJson.indexOf(searchKey)
            if (index == -1) return "System online. Aura servers acknowledged. Ready for next prompt."
            
            val contentStart = idxOfQuoteAfterColon(rawJson, index + searchKey.length)
            if (contentStart == -1) return "Signal error. Parsing failure."
            
            val contentEnd = rawJson.indexOf("\"", contentStart)
            if (contentEnd == -1) return "Signal mismatch."
            
            rawJson.substring(contentStart, contentEnd)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
        } catch (e: Exception) {
            "Payload parsed."
        }
    }

    private fun idxOfQuoteAfterColon(text: String, startIdx: Int): Int {
        var idx = startIdx
        while (idx < text.length) {
            if (text[idx] == '\"') {
                return idx + 1
            }
            idx++
        }
        return -1
    }

    private fun getOfflineAuraAIResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("meta") || lower.contains("patch") || lower.contains("update") -> {
                "**Aura Meta Analysis:** The key trends in current team formats favor lightweight hardware (such as 54g gaming mice) and highly dense foaming chairs for optimal gameplay endurance!"
            }
            lower.contains("trivia") || lower.contains("fact") -> {
                "**Aura Retro Flash:** Did you know that Mario's original name was 'Jumpman' in Nintendo's 1981 classic, Donkey Kong? Play **Neon Snake** in the Arcade tab to capture your profile XP point levels!"
            }
            lower.contains("shop") || lower.contains("product") || lower.contains("recommend") -> {
                "**Aura Gear Expert:** For competitive FPS/Moba, low click-latency is essential. We recommend ordering the **Apex Drift Wireless Mouse** (reaches 8000Hz polling rate) in our store."
            }
            lower.contains("ludo") || lower.contains("game") || lower.contains("snake") -> {
                "**Aura Game Hub Advice:** To excel in **Neon Snake Arcade**, turn early before collision edges. For **Cyber Street Racer**, drift side-by-side to dodge red lanes!"
            }
            else -> {
                "🤖 **[Aura AI Sandbox]** Hello! I'm your offline companion running on sandboxed/fallback loops.\n\n" +
                "Try asking me about:\n" +
                "- *'Tell me a retro gaming trivia'* \n" +
                "- *'Which mouse is recommended for esports?'* \n" +
                "- *'Give me tips on Neon Snake Arcade'* \n\n" +
                "*(Admin note: Insert a valid GEMINI_API_KEY in the AI Studio Secrets panel to enable live responses!)*"
            }
        }
    }
}
