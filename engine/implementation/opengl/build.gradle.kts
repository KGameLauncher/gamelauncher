plugins {
    id("gamelauncher-kotlin")
}

dependencies {
    implementation(projects.engine.implementation.providers)
    api(projects.engine.implementation.core)
    api(libs.gles.docs)
}