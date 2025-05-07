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

import org.jetbrains.compose.compose
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ktlint)
}

kotlin {
    explicitApi()

    androidTarget { publishLibraryVariants("release") }
    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "zoomable"
            isStatic = true
        }
    }

    applyDefaultHierarchyTemplate {
        sourceSetTrees(KotlinSourceSetTree.main, KotlinSourceSetTree.test)
        common {
            group("nonAndroid") {
                withJvm()
                withIos()
                withWasmJs()
            }
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
            implementation(compose.foundation)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.uiUtil)

            implementation(libs.androidx.annotation)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))

            implementation(libs.kotlinx.coroutines.test)

            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)

            implementation(libs.compose.material.icons)
        }
        invokeWhenCreated("androidDebug") {
            dependencies {
                implementation(libs.compose.ui.test.manifest)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.uiTestJUnit4)
                implementation(libs.junit)
                implementation(libs.robolectric)
            }
        }
        val desktopTest by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

android {
    namespace = "net.engawapg.lib.zoomable"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dokka {
    dokkaSourceSets.commonMain {
        enableAndroidDocumentationLink = true
    }
    pluginsConfiguration.html {
        moduleVersion = rootProject.properties["VERSION_NAME"]!!.toString()
    }
    dokkaPublications.html {
        outputDirectory = file("$rootDir/docs")
    }
}
