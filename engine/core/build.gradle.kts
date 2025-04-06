plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

dependencies {
    implementation(projects.engine.implementation.providers)
    api(libs.bundles.logging)
    api(libs.joml)

    testApi(libs.kotlin.test)
}