plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    macosArm64 {
        binaries.executable {
            entryPoint = "net.engawapg.app.zoomable.main"

            // Compose resources are not bundled into a macOS executable, so place them
            // where the resource reader looks for them: the "compose-resources"
            // directory next to the executable.
            val binaryName = name
            val copyComposeResources = tasks.register<Copy>(
                "copyComposeResourcesFor${binaryName.replaceFirstChar { it.uppercase() }}MacosArm64"
            ) {
                from(tasks.named("macosArm64AggregateResources"))
                into(outputDirectory.resolve("compose-resources"))
            }
            linkTaskProvider.configure { finalizedBy(copyComposeResources) }
        }
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.samples.shared)
        implementation(libs.compose.ui)
    }
}
