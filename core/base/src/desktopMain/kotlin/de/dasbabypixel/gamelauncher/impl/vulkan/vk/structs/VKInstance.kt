package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.UTF8Strings
import de.dasbabypixel.gamelauncher.impl.vulkan.VKUtil
import de.dasbabypixel.gamelauncher.impl.vulkan.getVKLogger
import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import de.dasbabypixel.gamelauncher.util.GameException
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.EXTDebugUtils
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkAllocationCallbacks
import org.lwjgl.vulkan.VkApplicationInfo
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXTI
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT
import org.lwjgl.vulkan.VkExtensionProperties
import org.lwjgl.vulkan.VkInstance
import org.lwjgl.vulkan.VkInstanceCreateInfo
import org.lwjgl.vulkan.VkLayerProperties
import org.lwjgl.vulkan.VkPhysicalDevice

class VKInstance : AbstractGameResource {
    val pAllocator: VkAllocationCallbacks?
    val instance: VkInstance
    val validationLayersEnabled: Boolean
    private var messenger: Long = 0

    constructor(
        tracker: ResourceTracker,
        validationLayersEnabled: Boolean,
        stack: MemoryStack,
        pAllocator: VkAllocationCallbacks?,
        applicationInfo: VKApplicationInfo,
        enabledExtensionNames: Set<String>,
        enabledLayerNames: Set<String>
    ) : super(tracker) {
        this.pAllocator = pAllocator
        this.validationLayersEnabled = validationLayersEnabled
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
                    list.add(VKPhysicalDevice(this, physicalDevice))
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
            val createInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack)
                .`sType$Default`()
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
                    VkDebugUtilsMessengerCallbackDataEXT.calloc(stack)
                        .`sType$Default`()
                        .pMessage(stack.UTF8("Test message")))
            }
        }
    }

    override fun cleanup0(): CompletableFuture<Unit>? {
        VK10.vkDestroyInstance(instance, pAllocator)
        return null
    }

    companion object {
        private val logger by getVKLogger()
        val requiredInstanceExtensions = mutableSetOf<String>()
        val optionalInstanceExtensions = mutableSetOf<String>()
        val useLayers = setOf("VK_LAYER_KHRONOS_validation",
            "VK_LAYER_KHRONOS_synchronization2",
            "VK_LAYER_KHRONOS_profiles",
            "VK_LAYER_KHRONOS_shader_object")

        const val testVulkanDebugger = false

        fun createInstance(
            stack: MemoryStack,
            enableValidationLayers: Boolean,
            tracker: ResourceTracker,
            pAllocator: VkAllocationCallbacks?,
            extensions: Collection<String>
        ): VKInstance {
            val extraRequiredExtensions = mutableSetOf<String>()
            if (enableValidationLayers) {
                extraRequiredExtensions.add(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME)
            }
            val allRequiredExtensions =
                extensions.plus(requiredInstanceExtensions).plus(extraRequiredExtensions).toSet()
            val usedExtensions = createInstanceVerifyExtensions(stack,
                allRequiredExtensions,
                optionalInstanceExtensions)
            val enabledLayerNames = if (enableValidationLayers) {
                createInstanceEnableValidationLayers(stack)
            } else setOf()

            val vkApplicationInfo = VKApplicationInfo("Hello Triangle GameLauncher",
                VKVersion(1, 0, 0),
                "GameLauncher",
                VKVersion(1, 0, 0),
                VKApiVersion.V14)
            return VKInstance(tracker,
                enableValidationLayers,
                stack,
                pAllocator,
                vkApplicationInfo,
                usedExtensions,
                enabledLayerNames)
        }

        private fun createInstanceEnableValidationLayers(stack: MemoryStack): Set<String> {
            val pLayerCount = stack.callocInt(1)
            VK10.vkEnumerateInstanceLayerProperties(pLayerCount, null).vkValidate()
            val pProperties = VkLayerProperties.calloc(pLayerCount.get(0), stack)
            VK10.vkEnumerateInstanceLayerProperties(pLayerCount, pProperties).vkValidate()
            val availableLayers = mutableSetOf<String>()
            pProperties.forEach {
                val name = it.layerNameString()
                availableLayers.add(name)
                logger.debug("Detected layer {} (version {})", name, it.implementationVersion())
            }
            useLayers.subtract(availableLayers).forEach {
                logger.info("Missing validation layer {}", it)
            }
            val useLayers = availableLayers.filter(useLayers::contains).toSet()
            useLayers.forEach {
                logger.debug("Using layer {}", it)

            }
            return useLayers
        }

        private fun createInstanceVerifyExtensions(
            stack: MemoryStack, requiredExtensions: Set<String>, optionalExtensions: Set<String>
        ): Set<String> {
            val pExtensionCount = stack.callocInt(1)
            VK10.vkEnumerateInstanceExtensionProperties(null as CharSequence?,
                pExtensionCount,
                null).vkValidate()
            val pProperties = VkExtensionProperties.calloc(pExtensionCount.get(0), stack)
            VK10.vkEnumerateInstanceExtensionProperties(null as CharSequence?,
                pExtensionCount,
                pProperties).vkValidate()
            val availableExtensions = mutableSetOf<String>()
            pProperties.forEach {
                val name = it.extensionNameString()
                availableExtensions.add(name)
                logger.debug("Detected extension {} (version {})", name, it.specVersion())
            }
            return VKUtil.selectExtensions("instance",
                availableExtensions,
                requiredExtensions,
                optionalExtensions)
        }
    }
}
