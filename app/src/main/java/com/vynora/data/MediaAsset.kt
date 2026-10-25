package com.vynora.data

import kotlinx.serialization.Serializable

@Serializable enum class MediaKind { VIDEO, IMAGE, AUDIO }

/** Aset hasil upload. File disalin ke penyimpanan app, jadi proyek tetap utuh walau file asli dihapus. */
@Serializable data class MediaAsset(
    val id: String, val name: String, val kind: MediaKind,
    val path: String, val durationMs: Long = 0, val width: Int = 0, val height: Int = 0,
    val thumbPath: String? = null, val sizeBytes: Long = 0,
)
