package de.dasbabypixel.gamelauncher.util.debug

import de.dasbabypixel.gamelauncher.util.Config

object Debug {
    val debug: Boolean = Config.debug()
    val inIde: Boolean = Config.inIDE()
    val trackResources: Boolean = Config.trackResources()
    val calculateThreadStacks: Boolean = Config.calculateThreadStacks()
}
