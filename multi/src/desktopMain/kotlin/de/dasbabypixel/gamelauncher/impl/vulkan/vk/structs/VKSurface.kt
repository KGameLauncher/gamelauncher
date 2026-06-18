package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.LoadedDevice
import de.dasbabypixel.gamelauncher.impl.vulkan.VKLoaderSurface
import de.dasbabypixel.gamelauncher.impl.vulkan.VulkanInstance
import org.lwjgl.vulkan.KHRSurface

class VKSurface : AbstractGameResource {
    val instance: VKInstance
    val handle: Long
    val loadedDevice: LoadedDevice

    constructor(tracker: ResourceTracker, handle: Long, instance: VulkanInstance) : super(tracker) {
        this.handle = handle
        this.instance = instance.instance
        this.loadedDevice = VKLoaderSurface.loadForSurface(instance, tracker, this)
    }

    override fun cleanup0(): CompletableFuture<Unit> {
        KHRSurface.vkDestroySurfaceKHR(instance.instance, handle, instance.pAllocator)
        return loadedDevice.cleanupAsync()
    }
}
