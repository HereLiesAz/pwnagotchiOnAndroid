plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.hereliesaz.pwnagotchi.desktop"
version = "1.0.0"


dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
    implementation("org.java-websocket:Java-WebSocket:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3") // Matching a safe version
}

compose.desktop {
    application {
        mainClass = "com.hereliesaz.pwnagotchi.desktop.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Rpm)
            packageName = "pwnagotchi-desktop"
            packageVersion = "1.0.0"
        }
    }
}
