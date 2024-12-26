/*
 * Copyright 2022 usuiat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ktlint)
}

kotlin {
    androidTarget()

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    targets.all {
        compilations.all {
            kotlinOptions.let {
                if (it is KotlinJvmOptions) {
                    it.jvmTarget = "1.8"
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(path = ":zoomable"))
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.components.resources)
            implementation(compose.material3)

            implementation(libs.coil.compose)
            implementation(libs.coil.network)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)

                implementation(libs.kotlinx.coroutines.swing)

                implementation(libs.ktor.client.java)
            }
        }
        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.androidx.lifecycle)
            implementation(libs.androidx.activity)
            implementation(libs.ktor.client.android)
            implementation(libs.kotlinx.coroutines.android)
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        invokeWhenCreated("androidDebug") {
            dependencies {
                implementation(compose.uiTooling)
                implementation(libs.compose.ui.test.manifest)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "net.engawapg.app.zoomable"
            packageVersion = "1.0.0"
        }
    }
}

android {
    namespace = "net.engawapg.app.zoomable"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "net.engawapg.app.zoomable"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode =  1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
