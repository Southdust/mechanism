# ⚒ mechanism

![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white&color=0d1117)
![Discord](https://img.shields.io/discord/908438033613848596?style=for-the-badge&logo=discord&logoColor=white&colorA=0d1117&colorB=1a222e)
![CI](https://img.shields.io/github/workflow/status/playhexalite/mechanism/Kotlin%20CI%20with%20Gradle.svg?colorA=0d1117&colorB=1a222e&label=Workflow&style=for-the-badge&logo=githubactions&logoColor=white)
![Version](https://img.shields.io/nexus/s/org.hexalite/mechanism-core?server=https%3A%2F%2Fs01.oss.sonatype.org?colorA=0d1117&colorB=1a222e&label=Maven&style=for-the-badge&logo=maven&logoColor=white)

A multiplatform-friendly common utility module targeting code flexibility.

**⚠️ WARNING! This library depends on Kotlin's experimental language and API versions of 1.8. You can find an example
in how to enable it at `build-logic/src/main/kotlin/mechanism-mpp-conventions.gradle.kts`**

## 🎏 Getting Started

You can find information about how to use this library in the classes' documentation! We will go into more details as
soon our website is ready.

### 🐚 Installation

<sup><sub>replace x.y.z with the latest version</sub></sup>

**Gradle (Kotlin DSL)**
```
repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "SonatypeSnapshots"
    }
}

dependencies {
    implementation("org.hexalite", "mechanism-core", "x.y.z")
    implementation("org.hexalite", "mechanism-extension-arrow", "x.y.z") // arrow extensions, if needed
}
```

