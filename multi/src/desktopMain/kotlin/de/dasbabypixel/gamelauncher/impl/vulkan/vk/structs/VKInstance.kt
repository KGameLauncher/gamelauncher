package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.UTF8Strings
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

class VKInstance : AbstractGameResource {
    val pAllocator: VkAllocationCallbacks?
    val instance: VkInstance
    private var messenger: Long = 0
    val devices = mutableSetOf<VKDevice>()

    constructor(
        tracker: ResourceTracker,
        stack: MemoryStack,
        pAllocator: VkAllocationCallbacks?,
        applicationInfo: VKApplicationInfo,
        enabledExtensionNames: Set<String>,
        enabledLayerNames: Set<String>
    ) : super(tracker) {
        this.pAllocator = pAllocator
        val vkApplicationInfo = VkApplicationInfo.calloc(stack).apply {
            `sType$Default`()
            pApplicationName(stack.UTF8(applicationInfo.applicationName))
            applicationVersion(applicationInfo.applicationVersion.vkVersion)
            pEngineName(stack.UTF8(applicationInfo.engineName))
            engineVersion(applicationInfo.engineVersion.vkVersion)
            apiVersion(applicationInfo.apiVersion.vk)
        }
        val pEnabledLayerNames = stack.UTF8Strings(enabledLayerNames)
        val pEnabledExtensionNames = stack.UTF8Strings(enabledExtensionNames)

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

    fun destroyDebugMessenger() {
        EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, messenger, pAllocator)
    }

    fun setupDebugMessenger(
        callback: VkDebugUtilsMessengerCallbackEXTI
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
                pAllocator,
                pMessenger).vkValidate()
            messenger = pMessenger.get(0)

            if (testVulkanDebugger) {
                EXTDebugUtils.vkSubmitDebugUtilsMessageEXT(instance,
                    EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT,
                    EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT,
                    VkDebugUtilsMessengerCallbackDataEXT.calloc(stack).`sType$Default`()
                        .pMessage(stack.UTF8("Test message")))
            }
        }
    }

    override fun cleanup0(): CompletableFuture<Unit>? {
        devices.toList().map { it.cleanupAsync() }.forEach { it.join() }
        VK10.vkDestroyInstance(instance, pAllocator)
        return null
    }

    companion object {
        const val testVulkanDebugger = false
    }
}
