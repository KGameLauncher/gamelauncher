package de.dasbabypixel.gamelauncher.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

class GameLauncherKotlinPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.apply<JavaPlugin>()
        project.plugins.apply("org.jetbrains.kotlin.jvm")

        val java = project.extensions.getByType<JavaPluginExtension>()

        val kotlin = project.extensions.getByType<KotlinJvmExtension>()
        kotlin.jvmToolchain(25)

        project.tasks.withType<KotlinJvmCompile>().configureEach {
            jvmTargetValidationMode.set(JvmTargetValidationMode.IGNORE)
        }
    }
}
