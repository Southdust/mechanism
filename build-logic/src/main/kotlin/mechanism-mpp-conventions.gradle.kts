plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

val mechanism = the<org.gradle.accessors.dm.LibrariesForMechanism>()

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
                jvmTarget = "17"
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        nodejs()
    }
    targets.all {
        compilations.all {
            kotlinOptions {
                verbose = true
                apiVersion = "1.8"
                languageVersion = "1.8"
            }
        }
    }
    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(mechanism.kotlin.logging)
                implementation(mechanism.bundles.kotlin)
            }
        }
        named("commonTest") {
            dependencies {
                implementation(mechanism.bundles.kotest)
                implementation(mechanism.mockk)
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        named("jvmTest") {
            dependencies {
                implementation(mechanism.kotest.runner.junit5)
                implementation(mechanism.slf4j.simple)
            }
        }
    }
    explicitApi()
}
