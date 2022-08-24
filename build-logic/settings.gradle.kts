rootProject.name = "build-logic"

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    versionCatalogs {
        create("mechanism") {
            from(files("../mechanism.libs.toml"))
        }
    }
}
