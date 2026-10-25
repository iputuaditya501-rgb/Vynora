package com.vynora.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.vynora.data.*

private const val DP_PER_SEC = 60

@Composable
fun TimelineScreen(project: Project, selected: String?, onSelect: (String) -> Unit, onMove: (clipId: String, newStartMs: Long) -> Unit) {
    Column(Modifier.horizontalScroll(rememberScrollState()).padding(8.dp)) {
        project.tracks.forEach { t ->
            Box(Modifier.height(48.dp).width((project.durationMs / 1000f * DP_PER_SEC + 200).dp).padding(vertical = 2.dp)) {
                t.clips.forEach { c ->
                    var dx by remember(c.id) { mutableStateOf(0f) }
                    val w = ((c.outMs - c.inMs) / 1000f * DP_PER_SEC).dp
                    Box(Modifier.offset(x = (c.startMs / 1000f * DP_PER_SEC).dp + dx.dp).width(w).fillMaxHeight()
                        .background(trackColor(t.type), MaterialTheme.shapes.small)
                        .border(if (c.id == selected) 2.dp else 0.dp, Color.White, MaterialTheme.shapes.small)
                        .clickable { onSelect(c.id) }
                        .pointerInput(c.id) {
                            detectHorizontalDragGestures(onDragEnd = {
                                onMove(c.id, (c.startMs + dx / DP_PER_SEC * 1000).toLong().coerceAtLeast(0)); dx = 0f
                            }) { _, d -> dx += d / density }
                        }) { Text(c.text ?: t.type.name, Modifier.padding(4.dp), color = Color.White) }
                }
            }
        }
    }
}

private fun trackColor(t: TrackType) = when (t) {
    TrackType.VIDEO -> Color(0xFF3B82F6); TrackType.AUDIO -> Color(0xFF10B981)
    TrackType.TEXT -> Color(0xFFF59E0B); TrackType.STICKER -> Color(0xFFEC4899); TrackType.IMAGE -> Color(0xFF8B5CF6)
}
