package com.vynora.music

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class MusicTrack(val title: String, val artist: String, val url: String, val license: String)

/** Musik gratis global: Openverse (tanpa key, lisensi CC) + Jamendo (client_id gratis dari devportal.jamendo.com). */
class MusicRepo(private val http: OkHttpClient = OkHttpClient(), private val jamendoClientId: String? = null) {
    private fun get(url: String) = http.newCall(Request.Builder().url(url).header("User-Agent", "Vynora/1.0").build())
        .execute().use { it.body?.string().orEmpty() }
    private fun enc(s: String) = java.net.URLEncoder.encode(s, "UTF-8")

    fun openverse(q: String): List<MusicTrack> {
        val a = JSONObject(get("https://api.openverse.org/v1/audio/?q=${enc(q)}&page_size=20")).optJSONArray("results") ?: return emptyList()
        return (0 until a.length()).mapNotNull { i -> a.getJSONObject(i).let {
            val u = it.optString("url")
            if (u.isBlank()) null else MusicTrack(it.optString("title"), it.optString("creator"), u, "CC " + it.optString("license")) } }
    }

    fun jamendo(q: String): List<MusicTrack> {
        val id = jamendoClientId ?: return emptyList()
        val a = JSONObject(get("https://api.jamendo.com/v3.0/tracks/?client_id=$id&format=json&limit=20&audiodownload_allowed=true&search=${enc(q)}")).optJSONArray("results") ?: return emptyList()
        return (0 until a.length()).map { i -> a.getJSONObject(i).let {
            MusicTrack(it.getString("name"), it.getString("artist_name"), it.getString("audiodownload"), "Jamendo CC") } }
    }

    fun search(q: String) = runCatching { openverse(q) }.getOrDefault(emptyList()) + runCatching { jamendo(q) }.getOrDefault(emptyList())
}
