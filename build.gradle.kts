@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(mechanism.plugins.kotlin.multiplatform) apply false
    alias(mechanism.plugins.kotlin.serialization) apply false
    alias(mechanism.plugins.dokka) apply false
}

allprojects {
    repositories {
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
            name = "Sonatype"
        }
        mavenCentral()
    }
}
