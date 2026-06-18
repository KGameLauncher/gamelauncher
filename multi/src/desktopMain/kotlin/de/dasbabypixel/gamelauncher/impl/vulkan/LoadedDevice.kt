package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKDevice
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKQueue

class LoadedDevice(tracker: ResourceTracker, val device: VKDevice, val graphicsQueue: VKQueue) :
    AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit> {
        return device.cleanupAsync()
    }
}
