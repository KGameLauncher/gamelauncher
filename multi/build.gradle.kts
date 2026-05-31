import de.dasbabypixel.gamelauncher.gradle.lwjglDefaultDevArgs
import de.dasbabypixel.gamelauncher.gradle.lwjglDefaultDevInitSystemProperties
import de.dasbabypixel.gamelauncher.gradle.lwjglMain
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.nio.charset.Charset

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        namespace = "de.dasbabypixel.gamelauncher"
        compileSdk = 37
        minSdk = 24
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    sourceSets {
        commonMain.dependencies {
        }
        val jvmCommon by registering {
            dependsOn(commonMain.get())
        }
        val desktopMain by getting {
            dependsOn(jvmCommon.get())

            dependencies {
                api(libs.disruptor)
                api(libs.bundles.logging)
                api(libs.bundles.logging.runtime)
                api(libs.bundles.jline)
            }
        }
        val androidMain by getting {
            dependsOn(jvmCommon.get())
        }
    }
}

abstract class Template : JavaExec() {
    init {
        classpath(project.sourceSets.named("desktopMain").map { it.runtimeClasspath })
        javaLauncher = project.javaToolchains.launcherFor(project.java.toolchain)
        mainClass = lwjglMain
        workingDir(project.rootProject.mkdir("run"))
        jvmArgs(lwjglDefaultDevArgs)
        jvmArgs(lwjglDefaultDevInitSystemProperties.map { "-D${it.key}=${it.value}" })
        jvmArgs("-Dgamelauncher.skipsysprops=true")
        val charset = Charset.defaultCharset()
        jvmArgs("-Dstdout.encoding=${charset.name()}", "-Dstderr.encoding=${charset.name()}")
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