package de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs

import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.UTF8Strings
import de.dasbabypixel.gamelauncher.impl.vulkan.vkValidate
import de.dasbabypixel.gamelauncher.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.resource.ResourceTracker
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkAllocationCallbacks
import org.lwjgl.vulkan.VkDevice
import org.lwjgl.vulkan.VkDeviceCreateInfo
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo
import org.lwjgl.vulkan.VkPhysicalDeviceExtendedDynamicStateFeaturesEXT
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan13Features

class VKDevice(
    tracker: ResourceTracker,
    val instance: VKInstance,
    val device: VkDevice,
    val physicalDevice: VKPhysicalDevice,
    val pAllocator: VkAllocationCallbacks?
) : AbstractGameResource(tracker) {

    override fun cleanup0(): CompletableFuture<Unit>? {
        VK10.vkDestroyDevice(device, pAllocator)
        return null
    }

    fun waitIdle() {
        VK10.vkDeviceWaitIdle(device).vkValidate()
    }

    companion object {
        fun create(
            tracker: ResourceTracker,
            instance: VKInstance,
            physicalDevice: VKPhysicalDevice,
            deviceQueues: Collection<VKDeviceQueueCreateInfo>,
            selectedExtensions: Collection<String>
        ): VKDevice {
            return MemoryStack.stackPush().use { stack ->
                val queueInfos = VkDeviceQueueCreateInfo.calloc(deviceQueues.size, stack).`sType$Default`()
                for (indexed in deviceQueues.withIndex()) {
                    val v = indexed.value
                    queueInfos[indexed.index].also { info ->
                        info.`sType$Default`()
                        info.queueFamilyIndex(v.queueFamilyIndex)
                        info.pQueuePriorities(stack.floats(*v.queuePriorities.toFloatArray()))
                    }
                }

                val deviceCreateInfo = VkDeviceCreateInfo.calloc(stack)
                    .`sType$Default`()
                    .pNext(VkPhysicalDeviceFeatures2.calloc(stack).`sType$Default`())
                    .pNext(VkPhysicalDeviceVulkan11Features.calloc().`sType$Default`().shaderDrawParameters(true))
                    .pNext(VkPhysicalDeviceVulkan13Features.calloc(stack)
                        .`sType$Default`()
                        .synchronization2(true)
                        .dynamicRendering(true))
                    .pNext(VkPhysicalDeviceExtendedDynamicStateFeaturesEXT.calloc(stack)
                        .`sType$Default`()
                        .extendedDynamicState(true))
                    .pQueueCreateInfos(queueInfos)
                    .ppEnabledExtensionNames(stack.UTF8Strings(selectedExtensions))

                val pDevice = stack.mallocPointer(1)
                VK10.vkCreateDevice(physicalDevice.device, deviceCreateInfo, instance.pAllocator, pDevice).vkValidate()
                val device = VkDevice(pDevice.get(0), physicalDevice.device, deviceCreateInfo)
                VKDevice(tracker, instance, device, physicalDevice, instance.pAllocator)
            }
        }
    }
}
