plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    mingwX64("native")

    sourceSets {
        commonMain.dependencies {
            api(libs.bundles.logging)
        }
    }
}
