plugins {
    id("gamelauncher-lwjgl")
}

dependencies {
    implementation(projects.engine.implementation.providers)
    api(projects.engine.implementation.core)
}
