package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.resource.stopTracking
import de.dasbabypixel.gamelauncher.api.util.function.GameRunnable
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.api.util.stack.StackTrace

abstract class AbstractThreadTask(tracker: ResourceTracker, val thread: Thread) :
    AbstractGameResource(tracker), ThreadTask {
    companion object {
        val logger = getLogger<AbstractThreadTask>()
    }

    override val autoTrack: Boolean
        get() = customStart
    protected open val customStart: Boolean
        get() = false

    fun start() {
        if (customStart) {
            customStart()
        } else {
            track()
            thread.start()
            logger.info("Started thread ${thread.name}[${thread.group.name}]")
        }
    }

    protected open fun customStart() {
    }

    abstract fun run0()

    override fun run() {
        try {
            run0()
        } catch (e: Throwable) {
            logger.error("Uncaught exception in ${thread.name}", e)
            stopTracking(tracker)
        }
    }
}
