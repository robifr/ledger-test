/**
 * Copyright 2024 Robi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

buildscript {
  repositories {
    google()
    mavenCentral()
  }

  dependencies { classpath(libs.jetbrains.kotlin.gradle.plugin) }
}

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.diffplug.spotless) apply false
  alias(libs.plugins.google.dagger.hilt.android) apply false
  alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}

allprojects {
  apply<SpotlessPlugin>()
  extensions.configure<SpotlessExtension> {
    java {
      target("**/*.java")
      targetExclude("$layout.buildDirectory/**/*.java")
      googleJavaFormat()
      toggleOffOn()
      trimTrailingWhitespace()
      removeUnusedImports()
      licenseHeaderFile(file("${project.rootDir}/gradle/spotless/license_header.txt"))
    }

    kotlin {
      target("**/*.kt")
      targetExclude("$layout.buildDirectory/**/*.kt")
      ktfmt()
      toggleOffOn()
      trimTrailingWhitespace()
      licenseHeaderFile(file("${project.rootDir}/gradle/spotless/license_header.txt"))
    }

    kotlinGradle {
      target("*.gradle.kts")
      targetExclude("$layout.buildDirectory/**/*.gradle.kts")
      ktfmt()
      licenseHeaderFile(file("${project.rootDir}/gradle/spotless/license_header.txt"), "^\\w+")
    }

    javascript {
      target("**/*.js")
      targetExclude("**/assets/libs/**/*.js", "**/node_modules/**/*.js")
      prettier().config(mapOf("tabWidth" to 2, "useTabs" to false, "printWidth" to 100))
      licenseHeaderFile(
          file("${project.rootDir}/gradle/spotless/license_header.txt"), "\"use strict\"|^\\w+")
    }

    format("xml") {
      target("**/src/**/*.xml")
      targetExclude("$layout.buildDirectory/**/*.xml")

      // Set delimiter to match either xml tag or comment, to prevent comment being removed when
      // placed below xml header tag.
      // <xml .../><!-- Any comment here shouldn't be replaced with header license -->
      licenseHeaderFile(
          file("${project.rootDir}/gradle/spotless/license_header_xml.txt"), "^(<\\w+|<!--)")
    }

    format("html") {
      target("**/src/main/assets/**/*.html")
      prettier()
      licenseHeaderFile(
          file("${project.rootDir}/gradle/spotless/license_header_html.txt"), "<!DOCTYPE")
    }
  }

  gradle.projectsEvaluated() {
    tasks {
      withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
      }

      withType<Test>() {
        configureEach { useJUnitPlatform() }

        // https://stackoverflow.com/a/36130467
        testLogging {
          testLogging {
            // Set options for log level LIFECYCLE.
            events(
                TestLogEvent.FAILED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_OUT)

            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true

            // Set options for log level DEBUG and INFO.
            debug {
              events(
                  TestLogEvent.STARTED,
                  TestLogEvent.FAILED,
                  TestLogEvent.PASSED,
                  TestLogEvent.SKIPPED,
                  TestLogEvent.STANDARD_ERROR,
                  TestLogEvent.STANDARD_OUT)

              exceptionFormat = TestExceptionFormat.FULL
            }

            info.events = debug.events
            info.exceptionFormat = debug.exceptionFormat

            afterSuite(
                KotlinClosure2({ description: TestDescriptor, result: TestResult ->
                  if (description.parent != null) {
                    val output: String =
                        "Results: ${result.resultType} (${result.testCount} tests, " +
                            "${result.successfulTestCount} passed, " +
                            "${result.failedTestCount} failed, " +
                            "${result.skippedTestCount} skipped)"
                    val repeatedMinusSign: String = "-".repeat(output.length)

                    println("${repeatedMinusSign}\n${output}\n")
                  }
                }))
          }
        }
      }

      create<Delete>("clear") { delete = setOf(rootProject.layout.buildDirectory) }
    }
  }
}
