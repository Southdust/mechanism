import java.util.Properties

plugins {
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

project.extra.apply {
    val secrets = rootProject.file("local.properties")
    if (secrets.exists()) {
        Properties().apply { secrets.inputStream().use(::load) }.onEach { (k, v) ->
            project.extra[k.toString()] = v
        }
    } else {
        set("signing.keyId", System.getenv("SIGNING_KEY_ID"))
        set("signing.password", System.getenv("SIGNING_PASSWORD"))
        set("signing.secretKeyRingFile", System.getenv("SIGNING_SECRET_KEY_RING_FILE"))
        set("ossrhUsername", System.getenv("OSSRH_USERNAME"))
        set("ossrhPassword", System.getenv("OSSRH_PASSWORD"))
    }
    set("isReleaseVersion", !project.version.toString().endsWith("SNAPSHOT"))
}

tasks {
    register<Jar>("javadocJar") {
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
        from(dokkaHtml.get().outputDirectory)
    }
    withType<Jar> {
        manifest {
            attributes += sortedMapOf(
                "Built-By" to System.getProperty("user.name"),
                "Build-Jdk" to System.getProperty("java.version"),
                "Implementation-Version" to project.version,
                "Created-By" to "${GradleVersion.current()}",
                "Created-From" to run {
                    val child = Runtime.getRuntime().exec("git rev-parse --verify HEAD")
                    child.waitFor()
                    if (child.exitValue() == 0) {
                        child.inputStream.readAllBytes().decodeToString().trim()
                    } else {
                        "none"
                    }
                }
            )
        }
    }
}

publishing {
    repositories {
        System.getenv("GITHUB_ACTOR")?.let { actor ->
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/playhexalite/${rootProject.name}")
                credentials {
                    username = actor
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
        if (project.extra.has("ossrhUsername")) {
            maven {
                name = "Sonatype"
                url = if (project.extra["isReleaseVersion"] == "true") {
                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                } else {
                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                }
                credentials {
                    username = project.extra["ossrhUsername"].toString()
                    password = project.extra["ossrhPassword"].toString()
                }
            }
        }
    }
    publications.withType<MavenPublication> {
        artifact(tasks["javadocJar"])
        pom {
            name.set(project.name)
            url.set("https://git.hexalite.org/${rootProject.name}")
            licenses {
                license {
                    name.set("GNU Affero General Public License, Version 3.0")
                    url.set("https://www.gnu.org/licenses/agpl-3.0.en.html")
                }
            }
            developers {
                developer {
                    name.set("Pedro Henrique")
                    email.set("me@syntax.lol")
                    organization.set("github")
                    organizationUrl.set("https:///www.github.com")
                }
            }
            scm {
                connection.set("scm:git:git://https://git.hexalite.org/${rootProject.name}.git")
                developerConnection.set("scm:git:ssh://https://git.hexalite.org/${rootProject.name}.git")
                url.set("https://git.hexalite.org/${rootProject.name}/tree/dev/next")
            }
        }
    }
}

signing {
    if (project.extra.has("signing.keyId")) {
        sign(publishing.publications)
    }
}
