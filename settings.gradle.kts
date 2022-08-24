rootProject.name = "mechanism"

enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
    versionCatalogs {
        create("mechanism") {
            from(files("./mechanism.libs.toml"))
        }
    }
}

includeBuild("build-logic")
include(
    ":mechanism-core",
    ":mechanism-extension-arrow",
)
