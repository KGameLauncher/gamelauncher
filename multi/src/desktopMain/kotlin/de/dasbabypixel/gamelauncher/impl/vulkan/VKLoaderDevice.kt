package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.impl.vulkan.VKDeviceSelection.SelectedPhysicalDevice
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKDevice
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKInstance
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKQueue
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkDevice
import org.lwjgl.vulkan.VkDeviceCreateInfo
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo
import org.lwjgl.vulkan.VkPhysicalDeviceExtendedDynamicStateFeaturesEXT
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan13Features
import org.lwjgl.vulkan.VkQueue

object VKLoaderDevice {
    fun loadDevice(
        tracker: ResourceTracker, physicalDevice: SelectedPhysicalDevice, vkInstance: VKInstance
    ): LoadedDevice {
        MemoryStack.stackPush().use { stack ->

            val deviceQueueCreateInfo = VkDeviceQueueCreateInfo.calloc(stack)
                .`sType$Default`()
                .queueFamilyIndex(physicalDevice.graphicsQueueIndex)
                .pQueuePriorities(stack.floats(0.5F))

            val deviceCreateInfo = VkDeviceCreateInfo.calloc(stack)
                .`sType$Default`()
                .pNext(VkPhysicalDeviceFeatures2.calloc(stack).`sType$Default`())
                .pNext(VkPhysicalDeviceVulkan11Features.calloc()
                    .`sType$Default`()
                    .shaderDrawParameters(true))
                .pNext(VkPhysicalDeviceVulkan13Features.calloc(stack)
                    .`sType$Default`()
                    .dynamicRendering(true))
                .pNext(VkPhysicalDeviceExtendedDynamicStateFeaturesEXT.calloc(stack)
                    .`sType$Default`()
                    .extendedDynamicState(true))
                .pQueueCreateInfos(VkDeviceQueueCreateInfo.calloc(1, stack)
                    .`sType$Default`()
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

            return LoadedDevice(tracker, device, graphicsQueue)
        }
    }
}