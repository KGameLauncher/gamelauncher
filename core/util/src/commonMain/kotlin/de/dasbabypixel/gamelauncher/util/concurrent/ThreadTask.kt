package de.dasbabypixel.gamelauncher.util.concurrent

import de.dasbabypixel.gamelauncher.util.resource.GameResource

interface ThreadTask : GameResource {
    val thread: Thread

    fun run()

    fun start(internalStart: () -> Unit)

    fun start() = thread.start()
}
