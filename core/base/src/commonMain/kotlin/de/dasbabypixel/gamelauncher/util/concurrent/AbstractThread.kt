package de.dasbabypixel.gamelauncher.util.concurrent

import de.dasbabypixel.gamelauncher.logging.getLogger
import de.dasbabypixel.gamelauncher.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.resource.ResourceTracker

abstract class AbstractThreadTask(tracker: ResourceTracker, override val thread: Thread) :
    AbstractGameResource(tracker), ThreadTask {
    companion object {
        private val logger by getLogger()
    }

    override val autoTrack: Boolean
        get() = false
    protected open val customStart: Boolean
        get() = false

    override fun start(internalStart: () -> Unit) {
        if (customStart) {
            customStart(internalStart)
        } else {
            track(dropStack = 2u)
            internalStart()
            logger.debug("Started thread ${thread.name}[${thread.group.name}]")
        }
    }

    protected open fun customStart(internalStart: () -> Unit) {
    }

    abstract fun run0()

    final override fun run() {
        try {
            run0()
        } catch (e: Throwable) {
            logger.error("Uncaught exception in ${thread.name}", e)
            stopTracking()
        }
    }
}
