package de.dasbabypixel.gamelauncher.util

import de.dasbabypixel.gamelauncher.api.config.Config

object Debug {
    val debug = Config.DEBUG.value
    val calculateThreadStacks = Config.CALCULATE_THREAD_STACKS.value
    val inIde = Config.IN_IDE.value
    val trackResources = Config.TRACK_RESOURCES.value
}