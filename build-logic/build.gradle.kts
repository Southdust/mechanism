plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin", mechanism.versions.kotlin.get()))
    implementation(kotlin("serialization", mechanism.versions.kotlin.get()))
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:" + mechanism.plugins.dokka.get().version)
    implementation (files(mechanism.javaClass.superclass.protectionDomain.codeSource.location))
}
