package com.vynora.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.vynora.data.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

/** Upload: gambar (jpg/png/webp/gif), video (mp4/mov/mkv/webm/3gp), audio (mp3/wav/aac/m4a/ogg/flac). */
class MediaImporter(private val ctx: Context) {
    private val dir = File(ctx.filesDir, "media").apply { mkdirs() }
    private val thumbs = File(ctx.filesDir, "thumbs").apply { mkdirs() }
    private val index = File(ctx.filesDir, "media_index.json")
    private val json = Json { ignoreUnknownKeys = true }

    companion object { val MIME_TYPES = arrayOf("video/*", "image/*", "audio/*", "application/ogg") }

    fun library(): List<MediaAsset> = runCatching { json.decodeFromString<List<MediaAsset>>(index.readText()) }.getOrDefault(emptyList())
    private fun saveIndex(l: List<MediaAsset>) = index.writeText(json.encodeToString(l))

    fun import(uris: List<Uri>): List<MediaAsset> {
        val added = uris.mapNotNull { runCatching { importOne(it) }.getOrNull() }
        saveIndex(library() + added)
        return added
    }

    fun remove(a: MediaAsset) {
        File(a.path).delete(); a.thumbPath?.let { File(it).delete() }
        saveIndex(library().filterNot { it.id == a.id })
    }

    private fun importOne(uri: Uri): MediaAsset {
        val mime = ctx.contentResolver.getType(uri).orEmpty()
        val kind = when { mime.startsWith("video") -> MediaKind.VIDEO; mime.startsWith("image") -> MediaKind.IMAGE
            mime.startsWith("audio") || mime == "application/ogg" -> MediaKind.AUDIO; else -> error("Format tidak didukung: $mime") }
        var name = "media"; var size = 0L
        ctx.contentResolver.query(uri, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) { c.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let { name = c.getString(it) }
                c.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 }?.let { size = c.getLong(it) } } }
        val id = UUID.randomUUID().toString()
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: name.substringAfterLast('.', "bin")
        val out = File(dir, "$id.$ext")
        ctx.contentResolver.openInputStream(uri)!!.use { i -> out.outputStream().use { o -> i.copyTo(o) } }

        var dur = 0L; var w = 0; var h = 0; var thumb: File? = null
        if (kind == MediaKind.IMAGE) {
            val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }; BitmapFactory.decodeFile(out.path, o); w = o.outWidth; h = o.outHeight
            o.inJustDecodeBounds = false; o.inSampleSize = (maxOf(w, h) / 320).coerceAtLeast(1)
            BitmapFactory.decodeFile(out.path, o)?.let { thumb = saveThumb(id, it) }
        } else {
            val r = MediaMetadataRetriever()
            try {
                r.setDataSource(out.path)
                dur = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0
                if (kind == MediaKind.VIDEO) {
                    w = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
                    h = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
                    r.getFrameAtTime(0)?.let { thumb = saveThumb(id, Bitmap.createScaledBitmap(it, 320, (320f * it.height / it.width).toInt(), true)) }
                }
            } finally { r.release() }
        }
        return MediaAsset(id, name, kind, out.path, dur, w, h, thumb?.path, size)
    }

    private fun saveThumb(id: String, b: Bitmap) = File(thumbs, "$id.jpg").also { f -> f.outputStream().use { b.compress(Bitmap.CompressFormat.JPEG, 80, it) } }
}

/** Taruh aset ke timeline: video/gambar -> track visual, audio -> track audio, di akhir klip terakhir. */
fun Project.addAsset(a: MediaAsset): Project {
    val type = when (a.kind) { MediaKind.VIDEO -> TrackType.VIDEO; MediaKind.IMAGE -> TrackType.IMAGE; MediaKind.AUDIO -> TrackType.AUDIO }
    val len = if (a.kind == MediaKind.IMAGE) 3000L else a.durationMs
    val track = tracks.firstOrNull { it.type == type } ?: Track(UUID.randomUUID().toString(), type)
    val start = track.clips.maxOfOrNull { it.startMs + (it.outMs - it.inMs) } ?: 0L
    val clip = Clip(UUID.randomUUID().toString(), Uri.fromFile(File(a.path)).toString(), start, 0, len)
    val newTrack = track.copy(clips = track.clips + clip)
    return copy(tracks = if (tracks.any { it.id == track.id }) tracks.map { if (it.id == track.id) newTrack else it } else tracks + newTrack)
}
