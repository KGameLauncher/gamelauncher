plugins {
    id("gamelauncher-kotlin")
}

dependencies {
    implementation(projects.engine.implementation.providers)
    api(projects.engine.core)
}
