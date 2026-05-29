package de.dasbabypixel.gamelauncher.api.resource

import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.concurrent.concurrentSet
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger

class ResourceTracker(val enabled: Boolean) {
    private val resources = concurrentSet<GameResource>()
    private val logger = getLogger<ResourceTracker>()

    fun startTrackingResource(resource: GameResource) {
        if (!enabled) return
        resources.add(resource)
    }

    fun stopTrackingResource(resource: GameResource) {
        if (!enabled) return
        resources.remove(resource)
    }

    fun GameResource.startTracking() {
        startTrackingResource(this)
    }

    fun GameResource.stopTracking() {
        stopTrackingResource(this)
    }

    fun exit() {
        if (!enabled) return
        for (resource in resources) {
            if (resource is GameResource.StackCapable) {
                val ex = GameException("Stack: ${resource.creationThreadName}")
                resource.creationStack?.let { ex.stacktrace = it }

                logger.error("Memory Leak: {}", resource, ex)
            } else {
                logger.error("Memory Leak: {}", resource)
            }
        }
    }
}

fun GameResource.startTracking(tracker: ResourceTracker) {
    tracker.startTrackingResource(this)
}

fun GameResource.stopTracking(tracker: ResourceTracker) {
    tracker.stopTrackingResource(this)
}
