plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
}

repositories {
    gradlePluginPortal()
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("gamelauncherParent") {
            id = "gamelauncher-parent"
            implementationClass = "de.dasbabypixel.gamelauncher.gradle.GameLauncherParent"
        }
        register("gamelauncherLWJGL") {
            id = "gamelauncher-lwjgl"
            implementationClass = "de.dasbabypixel.gamelauncher.gradle.GameLauncherLWJGL"
        }
        register("gamelauncherKotlin") {
            id = "gamelauncher-kotlin"
            implementationClass = "de.dasbabypixel.gamelauncher.gradle.GameLauncherKotlinPlugin"
        }
    }
}