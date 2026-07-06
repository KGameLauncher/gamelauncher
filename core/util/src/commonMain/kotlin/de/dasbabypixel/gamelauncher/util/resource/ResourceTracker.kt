package de.dasbabypixel.gamelauncher.util.resource

import de.dasbabypixel.gamelauncher.util.concurrent.concurrentSet

class ResourceTracker(val enabled: Boolean) {
    private val resources = concurrentSet<GameResource>()

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

    fun exit(): List<GameResourceLeak> {
        if (!enabled) return emptyList()
        return resources.map { GameResourceLeak(it) }.also { resources.clear() }
    }
}

fun GameResource.startTracking(tracker: ResourceTracker) {
    tracker.startTrackingResource(this)
}

fun GameResource.stopTracking(tracker: ResourceTracker) {
    tracker.stopTrackingResource(this)
}
