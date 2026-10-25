package com.vynora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vynora.data.*
import com.vynora.engine.addAsset
import com.vynora.ui.*
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(Modifier.fillMaxSize()) {
                    val store = remember { ProjectStore(this@MainActivity) }
                    var project by remember { mutableStateOf(store.list().firstOrNull() ?: Project(UUID.randomUUID().toString(), "Proyek Baru")) }
                    var selected by remember { mutableStateOf<String?>(null) }
                    fun update(p: Project) { project = p; store.save(p) }
                    Column(Modifier.fillMaxSize().statusBarsPadding()) {
                        Text("Vynora - ${project.name}", Modifier.padding(12.dp), style = MaterialTheme.typography.titleMedium)
                        Box(Modifier.weight(1f)) { MediaLibraryScreen { update(project.addAsset(it)) } }
                        Text("Timeline", Modifier.padding(start = 12.dp))
                        Box(Modifier.height(220.dp)) {
                            TimelineScreen(project, selected, { selected = it }) { id, start ->
                                update(project.copy(tracks = project.tracks.map { t -> t.copy(clips = t.clips.map { if (it.id == id) it.copy(startMs = start) else it }) }))
                            }
                        }
                    }
                }
            }
        }
    }
}
