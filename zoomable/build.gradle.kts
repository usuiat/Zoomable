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
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    androidTarget { publishLibraryVariants("release") }
    jvm("desktop")

    applyDefaultHierarchyTemplate {
        sourceSetTrees(KotlinSourceSetTree.main, KotlinSourceSetTree.test)
        common {
            group("nonAndroid") {
                withJvm()
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

            implementation(compose("org.jetbrains.compose.material:material-icons-core"))
        }
        androidMain.dependencies {
            implementation(libs.androidx.core)
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
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.androidx.test.ext)
                implementation(libs.androidx.test.espresso)
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
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro")
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

tasks.dokkaHtml.configure {
    outputDirectory.set(file("$rootDir/docs"))
    val versionName = rootProject.properties["VERSION_NAME"]!!.toString()
    moduleVersion.set(versionName)
    dokkaSourceSets {
        named("androidMain") {
            noAndroidSdkLink.set(false)
        }
    }
}
