# =========================================================
# WorkManager + Hilt (necesario para que LyricsDownloadWorker
# se pueda instanciar por reflexión en release; sin esto el
# trabajo falla al instante y la app muestra "descarga cancelada")
# =========================================================
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(...);
}
-keep class androidx.hilt.work.** { *; }
-keep interface androidx.hilt.work.** { *; }

# Clases generadas por Hilt (Dagger) para inyección de dependencias
-keep class dagger.hilt.** { *; }
-keep class dagger.internal.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep class **_HiltModules { *; }
-keep class **_HiltModules$* { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keepclasseswithmembers class * {
    @dagger.assisted.AssistedInject <init>(...);
}
-keep @dagger.assisted.AssistedFactory interface *

# =========================================================
# kotlinx.serialization (usado en el backup .json)
# =========================================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.example.sonorid.**$$serializer { *; }
-keepclassmembers class com.example.sonorid.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.sonorid.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# =========================================================
# Retrofit + OkHttp (APIs de LRCLIB y TheAudioDB)
# =========================================================
-keepattributes Signature, Exceptions
-keep,allowobfuscation interface com.example.sonorid.data.remote.**
-keep class com.example.sonorid.data.remote.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# =========================================================
# Room (entidades y DAOs)
# =========================================================
-keep class com.example.sonorid.data.local.db.** { *; }

# =========================================================
# Media3 / ExoPlayer
# =========================================================
-dontwarn androidx.media3.**

# =========================================================
# Modelos de dominio expuestos entre capas
# =========================================================
-keep class com.example.sonorid.domain.model.** { *; }