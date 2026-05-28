package de.dasbabypixel.gamelauncher.lwjgl.vulkan

import de.dasbabypixel.gamelauncher.lwjgl.window.LWJGLWindow
import de.dasbabypixel.gamelauncher.lwjgl.window.RenderImplementationRenderer
import de.dasbabypixel.gamelauncher.lwjgl.window.WindowRenderImplementation

object VKWindowRenderer : WindowRenderImplementation {
    override fun enable(window: LWJGLWindow): RenderImplementationRenderer {
        VKInitializer.init()
        return object : RenderImplementationRenderer {
            override fun render(
                window: LWJGLWindow, framebufferWidth: UInt, framebufferHeight: UInt
            ) {

            }
        }
    }

    override fun disable(
        window: LWJGLWindow, renderer: RenderImplementationRenderer
    ) {
    }
}
