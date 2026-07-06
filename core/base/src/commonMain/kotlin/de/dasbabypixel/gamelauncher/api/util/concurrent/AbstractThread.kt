package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.logging.LoggingInstance
import de.dasbabypixel.gamelauncher.logging.getLogger
import de.dasbabypixel.gamelauncher.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.util.concurrent.ThreadTask
import de.dasbabypixel.gamelauncher.util.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.util.resource.ResourceTracker

abstract class AbstractThreadTask(
    loggingInstance: LoggingInstance, tracker: ResourceTracker, override val thread: Thread
) : AbstractGameResource(tracker, false), ThreadTask {
    protected val logger by getLogger(loggingInstance)

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
