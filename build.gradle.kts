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

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

buildscript {
  repositories {
    google()
    mavenCentral()
  }

  dependencies { classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23") }
}

plugins {
  id("com.android.application") version "8.4.0" apply false
  id("com.android.library") version "8.4.0" apply false
  id("com.diffplug.spotless") version "6.25.0" apply false
  id("com.google.dagger.hilt.android") version "2.51.1" apply false
  kotlin("jvm") version "1.9.23"
}

allprojects {
  apply(plugin = "com.diffplug.spotless")
  configure<SpotlessExtension> {
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
      target("**/src/main/assets/**/*.js")
      targetExclude("**/src/main/assets/libs/**/*.js")
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
