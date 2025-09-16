enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositories {
        maven("https://central.sonatype.com/repository/maven-snapshots") {
            mavenContent {
                includeGroup("org.lwjgl")
                snapshotsOnly()
            }
        }
        mavenCentral()
        google()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

includeBuild("build-extensions")

rootProject.name = "gamelauncher"

include("engine")
include("engine:core")
include("engine:implementation:core")
include("engine:implementation:providers")
include("engine:implementation:opengl")
include("engine:implementation:lwjgl")
//include("executables")

val ideaVendor: String? = System.getProperty("idea.vendor.name")
val isAndroid = ideaVendor != "JetBrains" || file("include.android").exists()
logger.info("Running on Android: $isAndroid")
if (isAndroid) { // Optimize build times: don't include android in normal IntelliJ
    include("engine:implementation:android")
}
