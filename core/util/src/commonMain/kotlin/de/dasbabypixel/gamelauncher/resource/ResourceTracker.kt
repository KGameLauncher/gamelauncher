package de.dasbabypixel.gamelauncher.resource

interface ResourceTracker {
    val enabled: Boolean
    fun startTrackingResource(resource: GameResource)
    fun stopTrackingResource(resource: GameResource)
}

fun GameResource.startTracking(tracker: ResourceTracker) {
    tracker.startTrackingResource(this)
}

fun GameResource.stopTracking(tracker: ResourceTracker) {
    tracker.stopTrackingResource(this)
}
