import de.dasbabypixel.gamelauncher.gradle.lwjglDefaultDevArgs
import de.dasbabypixel.gamelauncher.gradle.lwjglDefaultDevInitSystemProperties
import de.dasbabypixel.gamelauncher.gradle.lwjglMain
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile
import java.nio.charset.Charset

plugins {
    alias(libs.plugins.kotlin.multiplatform)
//    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.shadow)
    id("gamelauncher-lwjgl")
}

tasks.withType<AbstractKotlinCompile<*>> {
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
}

tasks.withType<JavaExec>().configureEach {
    mainClass = lwjglMain
    workingDir(project.mkdir("run"))
    jvmArgs(lwjglDefaultDevArgs)
    jvmArgs(lwjglDefaultDevInitSystemProperties.map { "-D${it.key}=${it.value}" })
    jvmArgs("-Dgamelauncher.skipsysprops=true")
    val charset = Charset.defaultCharset()
    jvmArgs("-Dstdout.encoding=${charset.name()}", "-Dstderr.encoding=${charset.name()}")
    standardInput = System.`in`
    standardOutput = System.out
    errorOutput = System.err
}

kotlin {
//    android {
//        namespace = "de.dasbabypixel.gamelauncher"
//        compileSdk = 37
//        minSdk = 24
//        compilerOptions {
//            jvmTarget.set(JvmTarget.JVM_1_8)
//        }
//    }
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
            moduleName = "gamelauncher"
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.serviceLoader)
            api(projects.core.logging)
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
//        val androidMain by getting {
//            dependsOn(jvmCommon.get())
//        }
    }
}
