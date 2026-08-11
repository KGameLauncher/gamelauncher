package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import de.dasbabypixel.gamelauncher.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.KHRSurface
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR
import org.lwjgl.vulkan.VkSurfaceFormatKHR
import java.nio.IntBuffer

class VKSurface : AbstractGameResource {
    val instance: VKInstance
    val handle: Long

    constructor(tracker: ResourceTracker, handle: Long, instance: VKInstance) : super(tracker) {
        this.handle = handle
        this.instance = instance
    }

    fun surfaceCapabilities(device: VKDevice, surfaceCapabilities: VkSurfaceCapabilitiesKHR) {
        KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device.physicalDevice.device, handle, surfaceCapabilities)
            .vkValidate()
    }

    fun <T> availableFormats(
        device: VKDevice, stack: MemoryStack, func: (VkSurfaceFormatKHR.Buffer) -> T
    ): T {
        val pCount = stack.mallocInt(1)
        KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device.physicalDevice.device, handle, pCount, null).vkValidate()
        val pFormats = VkSurfaceFormatKHR.malloc(pCount.get(0), stack)
        KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device.physicalDevice.device, handle, pCount, pFormats)
            .vkValidate()
        return func(pFormats)
    }

    fun <T> availableFormats(device: VKDevice, func: (VkSurfaceFormatKHR.Buffer) -> T): T {
        return MemoryStack.stackPush().use { availableFormats(device, it, func) }
    }

    fun <T> availablePresentModes(device: VKDevice, func: (IntBuffer) -> T): T {
        return MemoryStack.stackPush().use { stack ->
            val pCount = stack.mallocInt(1)
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device.physicalDevice.device, handle, pCount, null)
                .vkValidate()
            val pModes = stack.mallocInt(pCount.get(0))
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device.physicalDevice.device, handle, pCount, pModes)
                .vkValidate()
            func(pModes)
        }
    }

    override fun cleanup0(): CompletableFuture<Unit>? {
        KHRSurface.vkDestroySurfaceKHR(instance.instance, handle, instance.pAllocator)
        return null
    }
}
