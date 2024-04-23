/**
 * Copyright (c) 2024 Robi
 *
 * Ledger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ledger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ledger. If not, see <https://www.gnu.org/licenses/>.
 */

import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
  kotlin("android")
  id("com.android.application")
  id("com.google.dagger.hilt.android")
  id("kotlin-parcelize")
}

android {
  compileSdk = 34
  namespace = "com.robifr.ledger"
  buildToolsVersion = "34.0.0"

  defaultConfig {
    applicationId = "com.robifr.ledger"
    minSdk = 30
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    multiDexEnabled = true

    javaCompileOptions {
      annotationProcessorOptions {
        arguments["room.schemaLocation"] = "$projectDir/schemas"
        arguments["room.incremental"] = "true"
        arguments["room.expandProjection"] = "true"
      }
    }
  }

  sourceSets {
    named("main") {
      res.srcDirs(
          listOf(
              file("src/main/res/layouts/").listFiles(),
              "src/main/res/layouts",
              file("src/main/res/drawables/icon/").listFiles(),
              file("src/main/res/drawables/image/").listFiles(),
              file("src/main/res/drawables/shape/").listFiles(),
              file("src/main/res/drawables/").listFiles(),
              "src/main/res/drawables",
              "src/main/res"))
    }
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
      manifestPlaceholders["app_name"] = "@string/app_name"
      proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
    }

    getByName("debug") {
      applicationIdSuffix = ".debug"
      isDebuggable = true
      manifestPlaceholders["app_name"] = "@string/app_name_debug"
    }
  }

  buildFeatures { viewBinding = true }

  packaging { resources.excludes.add("META-INF/LICENSE") }

  lint {
    ignoreWarnings = true
    checkAllWarnings = false
    showAll = true
    abortOnError = false
    disable.add("MenuTitle")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlin { jvmToolchain(17) }

  testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies {
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  implementation("androidx.webkit:webkit:1.10.0")

  implementation("androidx.room:room-runtime:2.6.1")
  annotationProcessor("androidx.room:room-compiler:2.6.1")

  implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
  implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

  implementation("com.google.android.material:material:1.11.0")

  implementation("com.google.dagger:hilt-android:2.51.1")
  annotationProcessor("com.google.dagger:hilt-android-compiler:2.51.1")

  implementation("androidx.core:core-ktx:1.12.0")
  implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
  implementation("org.jetbrains.kotlin:kotlin-test")

  testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")

  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

tasks.register<Exec>("downloadD3Js") {
  val version: String = "7.9.0"
  val url: String = "https://cdn.jsdelivr.net/npm/d3@$version/dist/d3.js"
  val dir: File = file("src/main/assets/libs/").apply { mkdirs() }
  val file: File = File(file("src/main/assets/libs/"), "d3.js")

  // Prevent re-downloading when rebuilding the project.
  onlyIf { !file.exists() || !file.readText().contains(version) }

  commandLine("curl", url, "-o", file.absolutePath)
}

tasks.named("preBuild").dependsOn("downloadD3Js")
