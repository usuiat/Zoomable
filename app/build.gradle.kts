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

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    androidTarget()
    jvm("desktop")

    targets.all {
        compilations.all {
            kotlinOptions.let {
                if (it is KotlinJvmOptions) {
                    it.jvmTarget = "1.8"
                }
            }
        }
    }

    applyDefaultHierarchyTemplate {
        sourceSetTrees(KotlinSourceSetTree.main, KotlinSourceSetTree.test)
        common {
            group("nonAndroid") {
                withJvm()
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

                implementation(libs.ktor.client.okhttp)
            }
        }
        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.androidx.lifecycle)
            implementation(libs.androidx.activity)

            implementation(libs.accompanist.pager)
            implementation(libs.accompanist.pager.indicators)

            implementation(libs.ktor.client.okhttp)

            implementation(libs.kotlinx.coroutines.android)
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.androidx.test.ext)
                implementation(libs.androidx.test.espresso)
            }
        }
        invokeWhenCreated("androidDebug") {
            dependencies {
                implementation(compose.uiTooling)
                implementation(libs.compose.ui.test.manifest)
            }
        }
    }
}

compose {
    desktop.application {
        mainClass = "net.engawapg.app.zoomable.MainKt"
    }
}

android {
    namespace = "net.engawapg.app.zoomable"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.engawapg.app.zoomable"
        minSdk = 21
        targetSdk = 34
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