package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.VKInitializer
import org.lwjgl.vulkan.KHRSurface

class VKSurface : AbstractGameResource {
    val instance: VKInstance
    val handle: Long

    constructor(tracker: ResourceTracker, handle: Long, instance: VKInstance) : super(tracker) {
        this.handle = handle
        this.instance = instance
        VKInitializer.loadForSurface(tracker, this)
    }

    override fun cleanup0(): CompletableFuture<Unit>? {
        KHRSurface.vkDestroySurfaceKHR(instance.instance, handle, instance.pAllocator)
        return null
    }
}
