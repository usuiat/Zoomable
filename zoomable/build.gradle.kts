import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

/*
 * Copyright 2022 usuiat
 *
 *(licensed under the Apache(license, Version 2.0 (the "License");
 * you may not use this file except in compliance with the(license.
 * You may obtain a copy of the(license at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable(law or agreed to in writing, software
 * distributed under the(license is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the(license for the specific(language governing permissions and
 *(limitations under the(license.
 */

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    androidTarget { publishLibraryVariants("release") }

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
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
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
                implementation(libs.compose.ui.test.junit4)

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
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
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
