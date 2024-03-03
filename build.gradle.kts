import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

buildscript {
  repositories {
    google()
    mavenCentral()
  }

  dependencies { classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0") }
}

plugins {
  id("com.android.application") version "8.1.0" apply false
  id("com.android.library") version "8.1.0" apply false
  id("com.diffplug.spotless") version "6.25.0" apply false
  kotlin("jvm") version "1.9.10"
}

allprojects {
  apply(plugin = "com.diffplug.spotless")
  configure<SpotlessExtension> {
    val license: String =
        """
          /**
           * Copyright (c) 2022-present Robi
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


          """
            .trimIndent()

    java {
      target("**/*.java")
      targetExclude("$layout.buildDirectory/**/*.java")
      googleJavaFormat()
      toggleOffOn()
      trimTrailingWhitespace()
      removeUnusedImports()
      licenseHeader(license)
    }

    kotlin {
      target("**/*.kt")
      targetExclude("$layout.buildDirectory/**/*.kt")
      ktfmt()
      toggleOffOn()
      trimTrailingWhitespace()
      licenseHeader(license)
    }

    kotlinGradle {
      target("*.gradle.kts")
      ktfmt()
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
