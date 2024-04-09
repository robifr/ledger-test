plugins {
  kotlin("android")
  id("com.android.application")
  id("com.google.dagger.hilt.android")
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

  implementation("androidx.room:room-runtime:2.6.0")
  annotationProcessor("androidx.room:room-compiler:2.6.0")

  implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
  implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

  implementation("com.google.android.material:material:1.11.0")

  implementation("com.google.dagger:hilt-android:2.44")
  annotationProcessor("com.google.dagger:hilt-android-compiler:2.44")

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
