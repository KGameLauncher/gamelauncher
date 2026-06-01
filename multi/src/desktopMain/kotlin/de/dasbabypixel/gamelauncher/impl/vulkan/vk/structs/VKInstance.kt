package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.impl.vulkan.getVKLogger
import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.EXTDebugUtils
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkAllocationCallbacks
import org.lwjgl.vulkan.VkApplicationInfo
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXTI
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT
import org.lwjgl.vulkan.VkInstance
import org.lwjgl.vulkan.VkInstanceCreateInfo
import org.lwjgl.vulkan.VkPhysicalDevice

class VKInstance {
    private val instance: VkInstance

    constructor(
        stack: MemoryStack,
        pAllocator: VkAllocationCallbacks?,
        applicationInfo: VKApplicationInfo,
        enabledExtensionNames: Set<String>,
        enabledLayerNames: Set<String>
    ) {
        val vkApplicationInfo = VkApplicationInfo.calloc(stack).apply {
            `sType$Default`()
            pApplicationName(stack.UTF8(applicationInfo.applicationName))
            applicationVersion(applicationInfo.applicationVersion.vkVersion)
            pEngineName(stack.UTF8(applicationInfo.engineName))
            engineVersion(applicationInfo.engineVersion.vkVersion)
            apiVersion(applicationInfo.apiVersion.vk)
        }
        val pEnabledLayerNames = stack.callocPointer(enabledLayerNames.size).apply {
            mark()
            for (name in enabledLayerNames) {
                put(stack.UTF8(name))
            }
            reset()
        }

        val pEnabledExtensionNames = stack.callocPointer(enabledExtensionNames.size).apply {
            mark()
            for (name in enabledExtensionNames) {
                put(stack.UTF8(name))
            }
            reset()
        }

        val createInfo = VkInstanceCreateInfo.calloc(stack).apply {
            `sType$Default`()
            pApplicationInfo(vkApplicationInfo)
            ppEnabledLayerNames(pEnabledLayerNames).ppEnabledExtensionNames(pEnabledExtensionNames)
        }
        instance = stack.callocPointer(1).let {
            VK10.vkCreateInstance(createInfo, pAllocator, it).vkValidate()
            VkInstance(it.get(0), createInfo)
        }
    }

    fun enumeratePhysicalDevices(): List<VKPhysicalDevice> {
        MemoryStack.stackPush().use { stack ->
            val pCount = stack.mallocInt(1)
            VK10.vkEnumeratePhysicalDevices(instance, pCount, null).vkValidate()
            val count = pCount.get(0)
            if (count == 0) throw GameException("Failed to find GPUs with Vulkan support!")
            val pPhysicalDevices = stack.mallocPointer(count)
            VK10.vkEnumeratePhysicalDevices(instance, pCount, pPhysicalDevices).vkValidate()

            return mutableListOf<VKPhysicalDevice>().also { list ->
                for (i in 0 until count) {
                    val pPhysicalDevice = pPhysicalDevices.get(i)
                    val physicalDevice = VkPhysicalDevice(pPhysicalDevice, instance)
                    list.add(VKPhysicalDevice(physicalDevice))
                }
            }
        }
    }

    fun setupDebugMessenger(
        callback: VkDebugUtilsMessengerCallbackEXTI, allocator: VkAllocationCallbacks?
    ) {
        val cb = VkDebugUtilsMessengerCallbackEXT.create(callback)
        MemoryStack.stackPush().use { stack ->
            val pMessenger = stack.longs(cb.address())
            val createInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack).`sType$Default`()
                .messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT or EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
                .messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT or EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT or EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT)
                .pfnUserCallback(cb)

            EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance,
                createInfo,
                allocator,
                pMessenger)

            if (testVulkanDebugger) {
                EXTDebugUtils.vkSubmitDebugUtilsMessageEXT(instance,
                    EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT,
                    EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT,
                    VkDebugUtilsMessengerCallbackDataEXT.calloc(stack).`sType$Default`()
                        .pMessage(stack.UTF8("Test message")))
            }
        }
    }

    companion object {
        const val testVulkanDebugger = false
        private val logger = getVKLogger()
    }
}
