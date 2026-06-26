import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.kotlin.multiplatform.library")
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
        commonMain.dependencies {}
        val jvmCommon by registering {
            dependsOn(commonMain.get())
        }
        val desktopMain by getting {
            dependsOn(jvmCommon.get())
        }
        val androidMain by getting {
            dependsOn(jvmCommon.get())
        }
    }
}
