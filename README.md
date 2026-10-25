# Vynora (Kotlin, Android, Jetpack Compose)
Editor video + gambar + logo ala CapCut. Inti proyek ini sudah berisi:
- data/Models.kt      : model Project, Track, Clip, Effect, Transition
- data/ProjectStore.kt: simpan/muat proyek ke penyimpanan lokal (JSON)
- engine/Exporter.kt  : ekspor video dengan Media3 Transformer
- music/MusicRepo.kt  : musik gratis global (Openverse + Jamendo)
- ai/AiClient.kt      : Groq / Claude / Gemini + API key sendiri + AI otomatis paham aplikasi
- ui/TimelineScreen.kt: timeline multi-track (Compose)

## build.gradle.kts (app) dependensi
implementation("androidx.media3:media3-transformer:1.4.1")
implementation("androidx.media3:media3-effect:1.4.1")
implementation("androidx.media3:media3-exoplayer:1.4.1")
implementation("androidx.media3:media3-ui:1.4.1")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
implementation("androidx.security:security-crypto:1.1.0-alpha06")
implementation("io.coil-kt:coil-compose:2.6.0")
plugins: kotlin-android, kotlin-serialization, compose
Permission: INTERNET, READ_MEDIA_VIDEO/IMAGES/AUDIO

## Roadmap berikutnya
Editor gambar/logo (kanvas + layer + shape + teks), keyframe, chroma key, auto-caption, filter LUT.

## Upload media
engine/MediaImporter.kt menyalin file pilihan user (video/gambar/audio) ke penyimpanan app, membuat thumbnail + membaca durasi.
ui/MediaLibraryScreen.kt: tombol upload + grid pustaka; Project.addAsset() menaruh aset ke timeline.
Ikon app: res/mipmap-* (dibuat dari gambar logo V), nama app: Vynora.

## Build tanpa Android Studio (GitHub Actions)
Push semua isi folder ini ke repo GitHub -> tab Actions -> "Build APK" -> Run workflow.
Selesai (~5-8 menit): buka run-nya -> Artifacts -> Vynora-debug-apk -> unduh, ekstrak, install APK.
