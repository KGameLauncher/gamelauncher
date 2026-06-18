package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.Config
import de.dasbabypixel.gamelauncher.api.util.debug.Debug
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKApiVersion
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKApplicationInfo
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKInstance
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKVersion
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.EXTDebugUtils
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkAllocationCallbacks
import org.lwjgl.vulkan.VkExtensionProperties
import org.lwjgl.vulkan.VkLayerProperties

class VKInitializerInstance {
    companion object {
        val logger by getVKLogger()
        val enableValidationLayers: Boolean = !Config.release() || Debug.debug
        val requiredInstanceExtensions = mutableSetOf<String>()
        val optionalInstanceExtensions = mutableSetOf<String>()
        val useLayers = setOf("VK_LAYER_KHRONOS_validation",
            "VK_LAYER_KHRONOS_synchronization2",
            "VK_LAYER_KHRONOS_profiles",
            "VK_LAYER_KHRONOS_shader_object")

        init {
            if (enableValidationLayers) {
                requiredInstanceExtensions.add(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME)
            }
        }
    }

    fun createInstance(
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
        return VKUtil.selectExtensions("instance",
            availableExtensions,
            requiredExtensions,
            optionalExtensions)
    }

}