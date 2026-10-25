package com.vynora.data

import kotlinx.serialization.Serializable

@Serializable enum class TrackType { VIDEO, AUDIO, TEXT, STICKER, IMAGE }

@Serializable data class Transition(val type: String = "fade", val durationMs: Long = 500) // fade, slide, zoom, wipe, dissolve

@Serializable data class Effect(val name: String, val params: Map<String, Float> = emptyMap()) // brightness, contrast, blur, grayscale, speed

@Serializable data class Clip(
    val id: String,
    val uri: String,
    val startMs: Long,
    val inMs: Long = 0,
    val outMs: Long,
    val volume: Float = 1f,
    val speed: Float = 1f,
    val text: String? = null,
    val x: Float = 0.5f, val y: Float = 0.5f, val scale: Float = 1f, val rotation: Float = 0f,
    val effects: List<Effect> = emptyList(),
    val transitionIn: Transition? = null,
)

@Serializable data class Track(val id: String, val type: TrackType, val clips: List<Clip> = emptyList(), val muted: Boolean = false)

@Serializable data class Project(
    val id: String, val name: String,
    val width: Int = 1080, val height: Int = 1920, val fps: Int = 30,
    val tracks: List<Track> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis(),
) { val durationMs get() = tracks.flatMap { it.clips }.maxOfOrNull { it.startMs + (it.outMs - it.inMs) } ?: 0L }
