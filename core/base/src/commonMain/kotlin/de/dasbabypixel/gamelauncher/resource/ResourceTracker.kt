package de.dasbabypixel.gamelauncher.resource

import de.dasbabypixel.gamelauncher.logging.getLogger
import de.dasbabypixel.gamelauncher.util.GameException
import de.dasbabypixel.gamelauncher.util.concurrent.concurrentSet
import de.dasbabypixel.gamelauncher.util.debug.Debug

class SimpleResourceTracker(override val enabled: Boolean = Debug.trackResources) : ResourceTracker {
    private val resources = concurrentSet<GameResource>()
    private val logger by getLogger()

    override fun startTrackingResource(resource: GameResource) {
        if (!enabled) return
        resources.add(resource)
    }

    override fun stopTrackingResource(resource: GameResource) {
        if (!enabled) return
        resources.remove(resource)
    }

    fun exit() {
        if (!enabled) return
        for (resource in resources) {
            if (resource is GameResource.StackCapable) {
                val ex = GameException("Stack: ${resource.creationThreadName}")
                resource.creationStack?.let { ex.stackTrace = it }

                logger.error("Memory Leak: {}", resource, ex)
            } else {
                logger.error("Memory Leak: {}", resource)
            }
        }
    }

    companion object {
        val global: SimpleResourceTracker = SimpleResourceTracker()
    }
}
