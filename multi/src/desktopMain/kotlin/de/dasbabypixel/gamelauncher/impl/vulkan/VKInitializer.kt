package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.util.Config
import de.dasbabypixel.gamelauncher.api.util.GameException
import de.dasbabypixel.gamelauncher.api.util.debug.Debug
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKApiVersion
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKApplicationInfo
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKInstance
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKVersion
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.EXTDebugUtils
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkAllocationCallbacks
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT
import org.lwjgl.vulkan.VkExtensionProperties
import org.lwjgl.vulkan.VkLayerProperties

object VKInitializer {
    private val logger = getVKLogger()
    private val enableValidationLayers: Boolean = !Config.release() || Debug.debug
    private val useLayers = setOf("VK_LAYER_KHRONOS_validation",
        "VK_LAYER_KHRONOS_synchronization2",
        "VK_LAYER_KHRONOS_profiles",
        "VK_LAYER_KHRONOS_shader_object",
        "ILLEGAL_LAYER")
    private val requiredExtensions = mutableSetOf<String>()

    init {
        if (enableValidationLayers) {
            requiredExtensions.add(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME)
        }
    }

    fun init(extensions: Set<String>) {
        val pAllocator: VkAllocationCallbacks? = null

        val vkInstance = MemoryStack.stackPush().use { stack ->
            createInstance(pAllocator, extensions)
        }

        if (enableValidationLayers) {
            setupDebugMessenger(vkInstance, pAllocator)
        }

        MemoryStack.stackPush().use { stack ->
            val physicalDevices = vkInstance.enumeratePhysicalDevices()
            val bestDevice = physicalDevices.map { device ->
                val properties = device.properties(stack).properties
                val features = device.features(stack).features
                logger.debug("Detected GPU: {}", properties.deviceNameString())
                var score = 0

                // Discrete GPUs have a significant performance advantage
                if (properties.deviceType() == VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
                    score += 1000
                }

                // Maximum possible size of textures affects graphics quality
                score += properties.limits().maxImageDimension2D();

                // Application can't function without geometry shaders
                if (!features.geometryShader()) {
                    return@map null
                }
                device to score
            }.filterNotNull().maxBy { it.second }

            logger.info("Selected GPU: {}", bestDevice.first.properties(stack).name)
        }
    }

    private fun setupDebugMessenger(instance: VKInstance, allocator: VkAllocationCallbacks?) {
        instance.setupDebugMessenger({ severity, type, pCallbackData, pUserData ->
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
        }, allocator)
    }

    private fun createInstance(
        pAllocator: VkAllocationCallbacks?, extensions: Set<String>
    ): VKInstance {
        val allExtensions = extensions.plus(requiredExtensions)
        createInstanceVerifyExtensions(allExtensions)
        val enabledLayerNames = if (enableValidationLayers) {
            createInstanceEnableValidationLayers()
        } else setOf()

        val vkApplicationInfo = VKApplicationInfo("Hello Triangle GameLauncher",
            VKVersion(1, 0, 0),
            "GameLauncher",
            VKVersion(1, 0, 0),
            VKApiVersion.V14)
        return VKInstance(MemoryStack.stackGet(),
            pAllocator,
            vkApplicationInfo,
            allExtensions,
            enabledLayerNames);
    }

    private fun createInstanceEnableValidationLayers(): Set<String> {
        val pLayerCount = MemoryStack.stackGet().callocInt(1)
        VK10.vkEnumerateInstanceLayerProperties(pLayerCount, null).vkValidate()
        val pProperties = VkLayerProperties.calloc(pLayerCount.get(0), MemoryStack.stackGet())
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

    private fun createInstanceVerifyExtensions(extensions: Set<String>) {
        val pExtensionCount = MemoryStack.stackGet().callocInt(1)
        VK10.vkEnumerateInstanceExtensionProperties(null as CharSequence?, pExtensionCount, null)
            .vkValidate()
        val pProperties =
            VkExtensionProperties.calloc(pExtensionCount.get(0), MemoryStack.stackGet())
        VK10.vkEnumerateInstanceExtensionProperties(null as CharSequence?,
            pExtensionCount,
            pProperties).vkValidate()
        val availableExtensions = mutableSetOf<String>()
        pProperties.forEach {
            val name = it.extensionNameString()
            availableExtensions.add(name)
            logger.debug("Detected extension {} (version {})", name, it.specVersion())
        }

        val missingExtensions = mutableSetOf<String>()
        extensions.forEach { if (!availableExtensions.contains(it)) missingExtensions.add(it) }

        if (missingExtensions.isNotEmpty()) {
            throw GameException("Missing Vulkan Extensions: $missingExtensions")
        }
        extensions.forEach {
            logger.debug("Using extension {}", it)
        }
    }
}
