package com.vynora.engine

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.*
import androidx.media3.transformer.*
import com.vynora.data.*
import java.io.File

@UnstableApi
object Exporter {
    /** Susun timeline -> Composition (urutan video + layer audio) lalu ekspor MP4. */
    fun export(ctx: Context, p: Project, out: File, onDone: (Boolean) -> Unit) {
        val videoSeq = p.tracks.filter { it.type == TrackType.VIDEO || it.type == TrackType.IMAGE }
            .flatMap { it.clips }.sortedBy { it.startMs }.map { c ->
                val fx = c.effects.mapNotNull { e -> when (e.name) {
                    "brightness" -> Brightness(e.params["v"] ?: 0f)
                    "contrast" -> Contrast(e.params["v"] ?: 0f)
                    else -> null } }
                EditedMediaItem.Builder(
                    MediaItem.Builder().setUri(Uri.parse(c.uri))
                        .setClippingConfiguration(MediaItem.ClippingConfiguration.Builder()
                            .setStartPositionMs(c.inMs).setEndPositionMs(c.outMs).build()).build())
                    .setEffects(Effects(emptyList(), fx)).build()
            }
        val audio = p.tracks.filter { it.type == TrackType.AUDIO && !it.muted }.flatMap { it.clips }.map { c ->
            EditedMediaItem.Builder(MediaItem.fromUri(Uri.parse(c.uri))).build()
        }
        val sequences = buildList {
            add(EditedMediaItemSequence(videoSeq))
            if (audio.isNotEmpty()) add(EditedMediaItemSequence(audio))
        }
        val t = Transformer.Builder(ctx).setVideoMimeType("video/avc").setAudioMimeType("audio/mp4a-latm")
            .addListener(object : Transformer.Listener {
                override fun onCompleted(c: Composition, r: ExportResult) = onDone(true)
                override fun onError(c: Composition, r: ExportResult, e: ExportException) = onDone(false)
            }).build()
        t.start(Composition.Builder(sequences).build(), out.absolutePath)
    }
}
