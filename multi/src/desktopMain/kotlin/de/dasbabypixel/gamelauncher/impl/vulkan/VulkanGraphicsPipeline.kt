package de.dasbabypixel.gamelauncher.impl.vulkan

import de.dasbabypixel.gamelauncher.api.resource.AbstractGameResource
import de.dasbabypixel.gamelauncher.api.resource.ResourceTracker
import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKPipeline
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKPipelineLayout
import de.dasbabypixel.gamelauncher.impl.vulkan.vk.structs.VKShaderModule
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkExtent2D
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo
import org.lwjgl.vulkan.VkOffset2D
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo
import org.lwjgl.vulkan.VkPipelineRenderingCreateInfo
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo
import org.lwjgl.vulkan.VkRect2D
import org.lwjgl.vulkan.VkViewport

class VulkanGraphicsPipeline(
    tracker: ResourceTracker,
    val swapChain: VulkanSwapChain,
    val pipeline: VKPipeline,
    val pipelineLayout: VKPipelineLayout,
    val shaderModule: VKShaderModule
) : AbstractGameResource(tracker) {
    override fun cleanup0(): CompletableFuture<Unit> {
        return pipeline.cleanupAsync()
            .thenCompose { pipelineLayout.cleanupAsync() }
            .thenCompose { shaderModule.cleanupAsync() }
    }

    companion object {
        fun create(tracker: ResourceTracker, swapChain: VulkanSwapChain): VulkanGraphicsPipeline {
            val device = swapChain.swapChain.device
            val shaderModule = VKShaderModule.create(tracker, device, readShaderFile("slang.spv"))

            return MemoryStack.stackPush().use { stack ->
                val shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack).also { stages ->
                    stages.get(0).also { vert ->
                        vert.`sType$Default`()
                            .stage(VK10.VK_SHADER_STAGE_VERTEX_BIT)
                            .module(shaderModule.handle)
                            .pName(stack.UTF8("vertMain"))
                    }
                    stages.get(1).also { vert ->
                        vert.`sType$Default`()
                            .stage(VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
                            .module(shaderModule.handle)
                            .pName(stack.UTF8("fragMain"))
                    }
                }

                val dynamicStates = VkPipelineDynamicStateCreateInfo.calloc(stack).`sType$Default`()
                dynamicStates.pDynamicStates(stack.ints(VK10.VK_DYNAMIC_STATE_VIEWPORT,
                    VK10.VK_DYNAMIC_STATE_SCISSOR))

                val vertexInputInfo =
                    VkPipelineVertexInputStateCreateInfo.calloc(stack).`sType$Default`()
                val inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                    .`sType$Default`()
                    .topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
                val viewport = VkViewport.calloc(1, stack)
                    .x(0F)
                    .y(1F)
                    .width(swapChain.extent.x.toFloat())
                    .height(swapChain.extent.y.toFloat())
                    .minDepth(0F)
                    .maxDepth(1F)
                val scissor = VkRect2D.calloc(1, stack)
                    .offset(VkOffset2D.calloc(stack).set(0, 0))
                    .extent(VkExtent2D.calloc(stack).set(swapChain.extent.x, swapChain.extent.y))

                val viewportState = VkPipelineViewportStateCreateInfo.calloc(stack)
                    .`sType$Default`()
                    .viewportCount(1)
                    .pViewports(viewport)
                    .scissorCount(1)
                    .pScissors(scissor)

                val rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack)
                    .`sType$Default`()
                    .depthClampEnable(false)
                    .rasterizerDiscardEnable(false)
                    .polygonMode(VK10.VK_POLYGON_MODE_FILL)
                    .cullMode(VK10.VK_CULL_MODE_BACK_BIT)
                    .frontFace(VK10.VK_FRONT_FACE_CLOCKWISE)
                    .depthBiasEnable(false)
                    .lineWidth(1F)

                val multisampling = VkPipelineMultisampleStateCreateInfo.calloc(stack)
                    .`sType$Default`()
                    .rasterizationSamples(VK10.VK_SAMPLE_COUNT_1_BIT)
                    .sampleShadingEnable(false)

                val colorBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(1, stack)
                    .blendEnable(false)
                    .colorWriteMask(VK10.VK_COLOR_COMPONENT_R_BIT or VK10.VK_COLOR_COMPONENT_G_BIT or VK10.VK_COLOR_COMPONENT_B_BIT or VK10.VK_COLOR_COMPONENT_A_BIT)

                val colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack)
                    .`sType$Default`()
                    .logicOpEnable(false)
                    .logicOp(VK10.VK_LOGIC_OP_COPY)
                    .attachmentCount(1)
                    .pAttachments(colorBlendAttachment)

                val pipelineRenderingCreateInfo = VkPipelineRenderingCreateInfo.calloc(stack)
                    .`sType$Default`()
                    .colorAttachmentCount(1)
                    .pColorAttachmentFormats(stack.ints(swapChain.surfaceFormat.format))

                val pipelineLayout = VKPipelineLayout.create(tracker, device)

                val graphicsPipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(stack)
                    .`sType$Default`()
                    .stageCount(2)
                    .pStages(shaderStages)
                    .pVertexInputState(vertexInputInfo)
                    .pInputAssemblyState(inputAssembly)
                    .pViewportState(viewportState)
                    .pRasterizationState(rasterizer)
                    .pMultisampleState(multisampling)
                    .pColorBlendState(colorBlending)
                    .pDynamicState(dynamicStates)
                    .layout(pipelineLayout.handle)
                    .renderPass(0L)
                    .pNext(pipelineRenderingCreateInfo)

                val graphicsPipeline =
                    VKPipeline.create(tracker, device, graphicsPipelineCreateInfo)

                VulkanGraphicsPipeline(tracker,
                    swapChain,
                    graphicsPipeline,
                    pipelineLayout,
                    shaderModule)
            }
        }

        fun readShaderFile(path: String): ByteArray {
            return VulkanGraphicsPipeline::class.java.classLoader.getResourceAsStream(path)!!
                .readBytes()
        }
    }
}