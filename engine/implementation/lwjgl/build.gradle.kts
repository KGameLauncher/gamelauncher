import de.dasbabypixel.gamelauncher.gradle.lwjglDefaultDevArgs
import de.dasbabypixel.gamelauncher.gradle.lwjglDefaultDevInitSystemProperties
import de.dasbabypixel.gamelauncher.gradle.lwjglMain
import java.nio.charset.Charset

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("gamelauncher-lwjgl")
}

dependencies {
    api(projects.engine.implementation.providers)
    api(projects.engine.implementation.core)
    api(projects.engine.implementation.opengl)

    api(libs.bundles.logging.runtime)
    api(libs.bundles.jline)
    api(libs.disruptor)

    //<editor-fold desc="api(LWJGL)...">
    api("org.lwjgl:lwjgl:${lwjgl.version}")
    api("org.lwjgl:lwjgl-glfw:${lwjgl.version}")
    api("org.lwjgl:lwjgl-sdl:${lwjgl.version}")
    api("org.lwjgl:lwjgl-opengl:${lwjgl.version}")
    api("org.lwjgl:lwjgl-opengles:${lwjgl.version}")
    api("org.lwjgl:lwjgl-stb:${lwjgl.version}")

    runtimeOnly("org.lwjgl:lwjgl:${lwjgl.version}:${lwjgl.natives}") {
        this.artifact {
            this.classifier = lwjgl.natives
        }
    }
    runtimeOnly("org.lwjgl:lwjgl-glfw:${lwjgl.version}:${lwjgl.natives}") {
        this.artifact {
            this.classifier = lwjgl.natives
        }
    }
    runtimeOnly("org.lwjgl:lwjgl-sdl:${lwjgl.version}:${lwjgl.natives}") {
        this.artifact {
            this.classifier = lwjgl.natives
        }
    }
    runtimeOnly("org.lwjgl:lwjgl-opengl:${lwjgl.version}:${lwjgl.natives}") {
        this.artifact {
            this.classifier = lwjgl.natives
        }
    }
    runtimeOnly("org.lwjgl:lwjgl-opengles:${lwjgl.version}:${lwjgl.natives}") {
        this.artifact {
            this.classifier = lwjgl.natives
        }
    }
    runtimeOnly("org.lwjgl:lwjgl-stb:${lwjgl.version}:${lwjgl.natives}") {
        this.artifact {
            this.classifier = lwjgl.natives
        }
    }
    //</editor-fold>
}

kotlin {
    jvmToolchain(24)
}

java {
    targetCompatibility = JavaVersion.VERSION_23 // kotlin does not yet compile to jdk 24
    disableAutoTargetJvm()
}

abstract class Template : JavaExec() {
    init {
        classpath(project.sourceSets.main.map { it.runtimeClasspath })
        javaLauncher = project.javaToolchains.launcherFor(project.java.toolchain)
        mainClass = lwjglMain
        workingDir(project.rootProject.mkdir("run"))
        jvmArgs(lwjglDefaultDevArgs)
        jvmArgs(lwjglDefaultDevInitSystemProperties.map { "-D${it.key}=${it.value}" })
        jvmArgs("-Dgamelauncher.skipsysprops=true")
        val charset = Charset.defaultCharset()
        jvmArgs(
            "-Dstdout.encoding=${charset.name()}", "-Dstderr.encoding=${charset.name()}"
        )
        standardInput = System.`in`
        standardOutput = System.out
        errorOutput = System.err
    }
}

tasks {
    register<Template>("lwjglRun")
    listOf("sdl", "glfw").map { it.uppercase() }.forEach { windowSystem ->
        register<Template>("lwjglRun$windowSystem") {
            jvmArgs("-Dgamelauncher.window_system=${windowSystem.lowercase()}")
        }
    }
}