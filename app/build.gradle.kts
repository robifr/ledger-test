plugins {
  kotlin("android")
  id("com.android.application")
  id("kotlin-parcelize")
}

android {
  compileSdk = 34
  namespace = "com.robifr.ledger"
  buildToolsVersion = "33.0.1"

  defaultConfig {
    applicationId = "com.robifr.ledger"
    minSdk = 30
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    multiDexEnabled = true

    javaCompileOptions {
      annotationProcessorOptions {
        arguments.plus(
            mutableMapOf(
                "room.schemaLocation" to file("$projectDir/schemas").absolutePath,
                "room.incremental" to "true",
                "room.expandProjection" to "true"))
      }
    }
  }

  sourceSets {
    named("main") {
      res.srcDirs(
          listOf(
              file("src/main/res/layouts/").listFiles(),
              "src/main/res/layouts",
              file("src/main/res/drawables/icon/size_20").listFiles(),
              file("src/main/res/drawables/icon/size_32").listFiles(),
              file("src/main/res/drawables/icon/").listFiles(),
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

  implementation("androidx.room:room-runtime:2.6.0")
  annotationProcessor("androidx.room:room-compiler:2.6.0")

  implementation("com.google.android.material:material:1.10.0")

  implementation("androidx.core:core-ktx:1.12.0")
  implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
  implementation("org.jetbrains.kotlin:kotlin-test")

  testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")

  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
