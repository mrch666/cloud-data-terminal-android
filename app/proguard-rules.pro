# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.cloudterminal.data.remote.** { *; }
-keep class * extends retrofit2.Call { *; }

# Room
-keep class * extends androidx.room.RoomDatabase { *; }

# Hilt
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }

# CameraX
-keep class androidx.camera.** { *; }

# Compose
-keep class androidx.compose.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# OkHttp
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# WorkManager
-keep class androidx.work.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# ViewModel
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep application class
-keep public class com.cloudterminal.CloudTerminalApplication

# Keep all entry points
-keepclasseswithmembers class * {
    public static void main(java.lang.String[]);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Preserve annotated methods
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Preserve all model classes
-keep class com.cloudterminal.domain.models.** { *; }
-keep class com.cloudterminal.data.local.entity.** { *; }
-keep class com.cloudterminal.data.remote.dto.** { *; }