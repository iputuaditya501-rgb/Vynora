package com.vynora.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/** Semua proyek disimpan lokal: filesDir/projects/<id>.json */
class ProjectStore(ctx: Context) {
    private val dir = File(ctx.filesDir, "projects").apply { mkdirs() }
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun save(p: Project) = File(dir, "${p.id}.json").writeText(json.encodeToString(p.copy(updatedAt = System.currentTimeMillis())))
    fun load(id: String): Project? = File(dir, "$id.json").takeIf { it.exists() }?.let { json.decodeFromString(it.readText()) }
    fun list(): List<Project> = dir.listFiles { f -> f.extension == "json" }.orEmpty()
        .mapNotNull { runCatching { json.decodeFromString<Project>(it.readText()) }.getOrNull() }
        .sortedByDescending { it.updatedAt }
    fun delete(id: String) = File(dir, "$id.json").delete()
}
