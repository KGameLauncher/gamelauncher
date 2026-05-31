plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly("com.android.kotlin.multiplatform.library:com.android.kotlin.multiplatform.library.gradle.plugin:9.2.1")
//    compileOnly(libs.plugins.kotlin.multiplatform)
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