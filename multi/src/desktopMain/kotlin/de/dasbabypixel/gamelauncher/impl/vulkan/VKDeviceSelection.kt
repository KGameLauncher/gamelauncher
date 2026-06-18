package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKInstance
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKPhysicalDevice
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKSurface
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.KHRPortabilitySubset
import org.lwjgl.vulkan.KHRSurface
import org.lwjgl.vulkan.KHRSwapchain
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures
import org.lwjgl.vulkan.VkPhysicalDeviceProperties
import org.lwjgl.vulkan.VkQueueFamilyProperties

object VKDeviceSelection {
    private val requiredDeviceExtensions =
        mutableSetOf(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)
    private val optionalDeviceExtensions =
        mutableSetOf(KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME)
    private val logger by getVKLogger()

    private fun VkQueueFamilyProperties.supportsGraphics(): Boolean {
        return (queueFlags() and VK10.VK_QUEUE_GRAPHICS_BIT) == VK10.VK_QUEUE_GRAPHICS_BIT
    }

    private fun VkQueueFamilyProperties.canBeUsedForRendering(
        device: VKPhysicalDevice, queueFamilyIndex: Int, surface: VKSurface
    ): Boolean {
        if (!supportsGraphics()) return false
        val pSupported = MemoryUtil.memAllocInt(1)
        KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(device.device,
            queueFamilyIndex,
            surface.handle,
            pSupported).vkValidate()
        return pSupported.get(0) == VK10.VK_TRUE
    }

    fun selectBestPhysicalDevice(
        stack: MemoryStack, vkInstance: VKInstance, surface: VKSurface
    ): SelectedPhysicalDevice {
        val physicalDevices = vkInstance.enumeratePhysicalDevices()
        val properties = VkPhysicalDeviceProperties.malloc(stack)
        val features = VkPhysicalDeviceFeatures.malloc(stack);
        val bestDevice = physicalDevices.map { device ->
            MemoryStack.stackPush().use { stack ->
                val pProperties = device.getQueueFamilyProperties(stack)
                val graphicsQueue = pProperties.withIndex().first { idx ->
                    idx.value.canBeUsedForRendering(device, idx.index, surface)
                }

                val availableExtensions =
                    device.getExtensions(stack).map { it.extensionNameString() }.toSet()
                val missingExtensions = mutableSetOf<String>()
                missingExtensions.addAll(requiredDeviceExtensions)
                missingExtensions.removeAll(availableExtensions)
                if (missingExtensions.isNotEmpty()) {
                    logger.debug("Missing extensions: {}", missingExtensions)
                    return@use null
                }

                val selectedExtensions = VKUtil.selectExtensions("device",
                    availableExtensions,
                    requiredDeviceExtensions,
                    optionalDeviceExtensions)

                SelectedPhysicalDevice(device, graphicsQueue.index, selectedExtensions)
            }
        }.filterNotNull().map { device ->
            device.device.properties(properties)
            device.device.features(features)
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
        }.filterNotNull().maxBy { it.second }.first.also {
            it.device.properties(properties)
            it.device.features(features)
        }

        logger.info("Selected GPU: {}", properties.deviceNameString())

        return bestDevice
    }

    class SelectedPhysicalDevice(
        val device: VKPhysicalDevice,
        val graphicsQueueIndex: Int,
        val selectedExtensions: Set<String>
    )
}