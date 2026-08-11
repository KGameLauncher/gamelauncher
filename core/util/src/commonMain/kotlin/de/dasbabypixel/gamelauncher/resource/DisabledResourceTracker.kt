package de.dasbabypixel.gamelauncher.resource

object DisabledResourceTracker : ResourceTracker {
    override val enabled: Boolean
        get() = false

    override fun startTrackingResource(resource: GameResource) {
    }

    override fun stopTrackingResource(resource: GameResource) {
    }
}