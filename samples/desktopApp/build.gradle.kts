import org.gradle.kotlin.dsl.desktop
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    dependencies {
        implementation(projects.samples.composeApp)
        implementation(compose.desktop.currentOs)
        implementation(libs.ktor.client.java)
    }
}

compose.desktop {
    application {
        mainClass = "net.engawapg.app.zoomable.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "net.engawapg.app.zoomable"
            packageVersion = "1.0.0"
        }
    }
}

