package com.vynora.ai

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

enum class AiProvider(val label: String) { GROQ("Groq"), CLAUDE("Claude"), GEMINI("Gemini") }

/** Penjelasan aplikasi yang OTOMATIS dikirim ke AI, jadi user cukup prompt. */
val APP_BRIEF = """
Kamu adalah asisten di dalam aplikasi editor video Vynora (mirip CapCut).
Model data: Project -> Track(VIDEO/AUDIO/TEXT/STICKER/IMAGE) -> Clip(startMs,inMs,outMs,volume,speed,text,x,y,scale,rotation,effects,transitionIn).
Efek: brightness, contrast, blur, grayscale, speed. Transisi: fade, slide, zoom, wipe, dissolve.
Balas HANYA JSON berisi daftar aksi, contoh:
{"reply":"penjelasan singkat","actions":[
 {"op":"add_text","text":"Halo","startMs":0,"durationMs":3000},
 {"op":"add_transition","clipId":"c2","type":"fade","durationMs":500},
 {"op":"set_volume","clipId":"c1","value":0.5},
 {"op":"add_effect","clipId":"c1","name":"brightness","v":0.2},
 {"op":"trim","clipId":"c1","inMs":0,"outMs":5000},
 {"op":"search_music","query":"lofi chill"},
 {"op":"delete_clip","clipId":"c3"}]}
Gunakan hanya op di atas. Konteks proyek saat ini dikirim di setiap pesan.
""".trimIndent()

class AiClient(private val provider: AiProvider, private val apiKey: String, private val http: OkHttpClient = OkHttpClient()) {
    private val json = "application/json".toMediaType()

    fun ask(prompt: String, projectJson: String): String {
        val user = "Proyek: $projectJson\n\nPermintaan: $prompt"
        return when (provider) {
            AiProvider.GROQ -> groq(user)
            AiProvider.CLAUDE -> claude(user)
            AiProvider.GEMINI -> gemini(user)
        }
    }

    private fun call(req: Request) = http.newCall(req).execute().use { it.body?.string().orEmpty() }

    private fun groq(user: String): String {
        val body = JSONObject().put("model", "llama-3.3-70b-versatile").put("messages", JSONArray()
            .put(JSONObject().put("role", "system").put("content", APP_BRIEF))
            .put(JSONObject().put("role", "user").put("content", user)))
        val r = call(Request.Builder().url("https://api.groq.com/openai/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey").post(body.toString().toRequestBody(json)).build())
        return JSONObject(r).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
    }

    private fun claude(user: String): String {
        val body = JSONObject().put("model", "claude-sonnet-4-6").put("max_tokens", 2000).put("system", APP_BRIEF)
            .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", user)))
        val r = call(Request.Builder().url("https://api.anthropic.com/v1/messages")
            .header("x-api-key", apiKey).header("anthropic-version", "2023-06-01").post(body.toString().toRequestBody(json)).build())
        return JSONObject(r).getJSONArray("content").getJSONObject(0).getString("text")
    }

    private fun gemini(user: String): String {
        val body = JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", APP_BRIEF))))
            .put("contents", JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", user)))))
        val r = call(Request.Builder().url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")
            .header("x-goog-api-key", apiKey).post(body.toString().toRequestBody(json)).build())
        return JSONObject(r).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
    }
}
