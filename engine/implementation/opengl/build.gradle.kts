plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.engine.implementation.providers)
    api(projects.engine.implementation.core)
}