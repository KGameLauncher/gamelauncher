import de.dasbabypixel.gamelauncher.gradle.lwjglDefaultDevArgs
import de.dasbabypixel.gamelauncher.gradle.lwjglDefaultDevInitSystemProperties
import de.dasbabypixel.gamelauncher.gradle.lwjglMain
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile
import java.nio.charset.Charset

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.shadow)
    id("gamelauncher-lwjgl")
}

tasks.withType<AbstractKotlinCompile<*>> {
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
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
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.util)
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
                api(libs.kotlin.reflect)

                //<editor-fold desc="api(LWJGL)...">
                api("org.lwjgl:lwjgl:${lwjgl.version}")
                api("org.lwjgl:lwjgl-glfw:${lwjgl.version}")
                api("org.lwjgl:lwjgl-sdl:${lwjgl.version}")
                api("org.lwjgl:lwjgl-vulkan:${lwjgl.version}")
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
        outputs.upToDateWhen { false }
        standardInput = System.`in`
        standardOutput = System.out
        errorOutput = System.err
    }
}

tasks {
    listOf("sdl", "glfw").map { it.uppercase() }.forEach { windowSystem ->
        register<Template>("desktopRun$windowSystem") {
            group = "run"
            jvmArgs("-Dgamelauncher.window_system=${windowSystem.lowercase()}")
        }
    }
    named<Jar>("shadowJar") {
        this.manifest.attributes(mapOf("Main-Class" to "de.dasbabypixel.gamelauncher.impl.MainKt"))
        this.manifest.attributes(mapOf("Test" to "abc"))
        this.manifest.attributes(mapOf("Enable-Native-Access" to "ALL-UNNAMED"))
    }
}