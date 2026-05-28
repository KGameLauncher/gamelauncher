package de.dasbabypixel.gamelauncher.lwjgl.vulkan

import de.dasbabypixel.gamelauncher.lwjgl.vulkan.vk.structs.VKApiVersion
import de.dasbabypixel.gamelauncher.lwjgl.vulkan.vk.structs.VKApplicationInfo
import de.dasbabypixel.gamelauncher.lwjgl.vulkan.vk.structs.VKInstance
import de.dasbabypixel.gamelauncher.lwjgl.vulkan.vk.structs.VKVersion
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkAllocationCallbacks
import org.lwjgl.vulkan.VkExtensionProperties

object VKInitializer {
    fun init() {
        val pAllocator: VkAllocationCallbacks? = null

        val vkInstance = MemoryStack.stackPush().use { stack ->
            createInstance(stack, pAllocator)
        }
    }

    private fun createInstance(stack: MemoryStack, pAllocator: VkAllocationCallbacks?): VKInstance {

        val pExtensionCount = stack.callocInt(1)
        VK10.vkEnumerateInstanceExtensionProperties(null as CharSequence?, pExtensionCount, null)
        val pProperties = VkExtensionProperties.calloc(pExtensionCount.get(0), stack)
        VK10.vkEnumerateInstanceExtensionProperties(null as CharSequence?, pExtensionCount, pProperties)
        pProperties.forEach {
            println(it.extensionNameString())
        }

        val vkApplicationInfo = VKApplicationInfo("Hello Triangle GameLauncher",
            VKVersion(1, 0, 0),
            "GameLauncher",
            VKVersion(1, 0, 0),
            VKApiVersion.V14)
        return VKInstance(stack, pAllocator, vkApplicationInfo);
    }
}
