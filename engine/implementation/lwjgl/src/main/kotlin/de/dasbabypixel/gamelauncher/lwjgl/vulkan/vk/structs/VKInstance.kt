package de.dasbabypixel.gamelauncher.lwjgl.vulkan.vk.structs

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkAllocationCallbacks
import org.lwjgl.vulkan.VkApplicationInfo
import org.lwjgl.vulkan.VkInstanceCreateInfo

class VKInstance {
    private val ptr: Long

    constructor(stack: MemoryStack, pAllocator: VkAllocationCallbacks?, applicationInfo: VKApplicationInfo) {
        val vkApplicationInfo = VkApplicationInfo.calloc(stack).apply {
            `sType$Default`()
            pApplicationName(stack.UTF8(applicationInfo.applicationName))
            applicationVersion(applicationInfo.applicationVersion.vkVersion)
            pEngineName(stack.UTF8(applicationInfo.engineName))
            engineVersion(applicationInfo.engineVersion.vkVersion)
            apiVersion(applicationInfo.apiVersion.vk)
        }
        val createInfo = VkInstanceCreateInfo.calloc(stack).apply {
            `sType$Default`()
            pApplicationInfo(vkApplicationInfo)
        }
        ptr = stack.callocPointer(1).let {
            VK10.vkCreateInstance(createInfo, pAllocator, it)
            it.get(0)
        }
    }
}
