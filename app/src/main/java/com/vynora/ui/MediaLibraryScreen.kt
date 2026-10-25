package com.vynora.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vynora.data.*
import com.vynora.engine.MediaImporter
import java.io.File

@Composable
fun MediaLibraryScreen(onUse: (MediaAsset) -> Unit) {
    val ctx = LocalContext.current
    val importer = remember { MediaImporter(ctx) }
    var items by remember { mutableStateOf(importer.library()) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) { importer.import(uris); items = importer.library() }
    }
    Column(Modifier.fillMaxSize().padding(8.dp)) {
        Button({ picker.launch(MediaImporter.MIME_TYPES) }, Modifier.fillMaxWidth()) { Text("+ Upload video / gambar / audio") }
        LazyVerticalGrid(GridCells.Adaptive(96.dp), Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(items, key = { it.id }) { a ->
                Card(onClick = { onUse(a) }) {
                    Box(Modifier.height(96.dp).fillMaxWidth()) {
                        if (a.thumbPath != null) AsyncImage(File(a.thumbPath), a.name, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
                        else Text("♪ ${a.name}", Modifier.padding(6.dp), maxLines = 3)
                    }
                    Text(a.kind.name, Modifier.padding(4.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
