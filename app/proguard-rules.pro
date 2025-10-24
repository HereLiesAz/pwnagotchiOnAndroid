# Kotlin
-dontwarn kotlin.reflect.jvm.internal.**

# Kotlinx Serialization
-keepattributes *Annotation*
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable <methods>;
}
-keepclassmembers class **$$serializer {
    <methods>;
}

# Ktor
-keep class io.ktor.client.engine.cio.** { *; }
-keep class io.ktor.client.plugins.websocket.** { *; }
-keep class io.ktor.client.plugins.contentnegotiation.** { *; }
-keep class io.ktor.serialization.kotlinx.json.** { *; }
-dontwarn io.ktor.**
-dontwarn org.slf4j.**

# libsu
-keep class com.topjohnwu.superuser.** { *; }

# Jetpack Compose
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class androidx.compose.runtime.** { *; }

# Jetpack Glance
-keep class androidx.glance.appwidget.** { *; }
-keepclassmembers class androidx.glance.appwidget.** { *; }

# Java-WebSocket
-keep class org.java_websocket.** { *; }
