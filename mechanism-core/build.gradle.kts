plugins {
    id("mechanism-mpp-conventions")
    id("mechanism-publishing-conventions")
}

kotlin {
    sourceSets.named("commonMain") {
        dependencies {
            implementation(mechanism.uuid)
        }
    }
}
