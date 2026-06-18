package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.Config
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.debug.Debug
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.impl.vulkan.VKDeviceSelection.SelectedPhysicalDevice
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKApiVersion
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKApplicationInfo
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKDevice
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKInstance
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKQueue
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKSurface
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKVersion
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.EXTDebugUtils
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkAllocationCallbacks
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT
import org.lwjgl.vulkan.VkDevice
import org.lwjgl.vulkan.VkDeviceCreateInfo
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo
import org.lwjgl.vulkan.VkExtensionProperties
import org.lwjgl.vulkan.VkLayerProperties
import org.lwjgl.vulkan.VkPhysicalDeviceExtendedDynamicStateFeaturesEXT
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan13Features
import org.lwjgl.vulkan.VkQueue

object VKInitializer {
    private val logger by getLogger("Vulkan")
    private val enableValidationLayers: Boolean = !Config.release() || Debug.debug
    private val useLayers = setOf("VK_LAYER_KHRONOS_validation",
        "VK_LAYER_KHRONOS_synchronization2",
        "VK_LAYER_KHRONOS_profiles",
        "VK_LAYER_KHRONOS_shader_object",
        "ILLEGAL_LAYER")
    private val requiredInstanceExtensions = mutableSetOf<String>()
    private val optionalInstanceExtensions = mutableSetOf<String>()
    var vkInstance: VKInstance? = null

    init {
        if (enableValidationLayers) {
            requiredInstanceExtensions.add(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME)
        }
    }

    fun exit() {
        if (enableValidationLayers) {
            vkInstance?.destroyDebugMessenger()
        }
        vkInstance?.cleanupAsync()?.join()
    }

    fun init(tracker: ResourceTracker, extensions: Collection<String>) {
        val pAllocator: VkAllocationCallbacks? = null

        val vkInstance = MemoryStack.stackPush().use { stack ->
            createInstance(stack, tracker, pAllocator, extensions)
        }.also { this.vkInstance = it }

        if (enableValidationLayers) {
            setupDebugMessenger(vkInstance)
        }
    }

    fun loadForSurface(tracker: ResourceTracker, surface: VKSurface): LoadedDevice {
        val vkInstance = vkInstance!!
        MemoryStack.stackPush().use { stack ->
            val bestPhysicalDevice =
                VKDeviceSelection.selectBestPhysicalDevice(stack, vkInstance, surface)

            return loadDevice(tracker, bestPhysicalDevice, vkInstance)
        }
    }

    private fun loadDevice(
        tracker: ResourceTracker, physicalDevice: SelectedPhysicalDevice, vkInstance: VKInstance
    ): LoadedDevice {
        MemoryStack.stackPush().use { stack ->

            val deviceQueueCreateInfo = VkDeviceQueueCreateInfo.calloc(stack).`sType$Default`()
                .queueFamilyIndex(physicalDevice.graphicsQueueIndex)
                .pQueuePriorities(stack.floats(0.5F))

            val deviceCreateInfo = VkDeviceCreateInfo.calloc(stack).`sType$Default`()
                .pNext(VkPhysicalDeviceFeatures2.calloc(stack).`sType$Default`())
                .pNext(VkPhysicalDeviceVulkan13Features.calloc(stack).`sType$Default`()
                    .dynamicRendering(true))
                .pNext(VkPhysicalDeviceExtendedDynamicStateFeaturesEXT.calloc(stack)
                    .`sType$Default`().extendedDynamicState(true))
                .pQueueCreateInfos(VkDeviceQueueCreateInfo.calloc(1, stack).`sType$Default`()
                    .put(0, deviceQueueCreateInfo))
                .ppEnabledExtensionNames(stack.UTF8Strings(physicalDevice.selectedExtensions))

            val pDevice = stack.mallocPointer(1)
            VK10.vkCreateDevice(physicalDevice.device.device,
                deviceCreateInfo,
                vkInstance.pAllocator,
                pDevice).vkValidate()

            val device = VKDevice(tracker = tracker,
                vkInstance,
                VkDevice(pDevice.get(0), physicalDevice.device.device, deviceCreateInfo),
                vkInstance.pAllocator)


            val pQueue = stack.mallocPointer(1)
            VK10.vkGetDeviceQueue(device.device, physicalDevice.graphicsQueueIndex, 0, pQueue)
            val graphicsQueue = VKQueue(VkQueue(pQueue.get(0), device.device))

            return LoadedDevice(device, graphicsQueue)
        }
    }

    private fun setupDebugMessenger(instance: VKInstance) {
        instance.setupDebugMessenger { severity, type, pCallbackData, pUserData ->
            val callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData)
            val message = callbackData.pMessageString()!!
            val typeString = when (type) {
                EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT -> "validation"
                EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT -> "performance"
                EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT -> "general"
                else -> "ILLEGAL"
            }
            logger.error("Validation layer: Type {} Message: {}",
                typeString,
                message,
                GameException())
            0
        }
    }

    private fun createInstance(
        stack: MemoryStack,
        tracker: ResourceTracker,
        pAllocator: VkAllocationCallbacks?,
        extensions: Collection<String>
    ): VKInstance {
        val allRequiredExtensions = extensions.plus(requiredInstanceExtensions).toSet()
        val usedExtensions =
            createInstanceVerifyExtensions(stack, allRequiredExtensions, optionalInstanceExtensions)
        val enabledLayerNames = if (enableValidationLayers) {
            createInstanceEnableValidationLayers(stack)
        } else setOf()

        val vkApplicationInfo = VKApplicationInfo("Hello Triangle GameLauncher",
            VKVersion(1, 0, 0),
            "GameLauncher",
            VKVersion(1, 0, 0),
            VKApiVersion.V14)
        return VKInstance(tracker,
            stack,
            pAllocator,
            vkApplicationInfo,
            usedExtensions,
            enabledLayerNames);
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
        VK10.vkEnumerateInstanceExtensionProperties(null as CharSequence?, pExtensionCount, null)
            .vkValidate()
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
        return selectExtensions("instance",
            availableExtensions,
            requiredExtensions,
            optionalExtensions)
    }

    fun selectExtensions(
        type: String,
        availableExtensions: Set<String>,
        requiredExtensions: Set<String>,
        optionalExtensions: Set<String>
    ): Set<String> {
        val missingExtensions = mutableSetOf<String>()
        requiredExtensions.forEach { if (!availableExtensions.contains(it)) missingExtensions.add(it) }

        if (missingExtensions.isNotEmpty()) {
            throw GameException("Missing Vulkan $type Extensions: $missingExtensions")
        }
        requiredExtensions.forEach {
            logger.debug("Using required $type extension {}", it)
        }

        val selectExtensions = mutableSetOf<String>()
        selectExtensions.addAll(requiredExtensions)
        selectExtensions.addAll(optionalExtensions.filter { availableExtensions.contains(it) }
            .also {
                logger.debug("Using optional $type extensions: {}", it)
            })
        return selectExtensions
    }

    class LoadedDevice(val device: VKDevice, val graphicsQueue: VKQueue) {
        fun cleanupAsync() = device.cleanupAsync()
    }
}
