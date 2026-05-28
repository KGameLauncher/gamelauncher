package de.dasbabypixel.gamelauncher.lwjgl.window

interface WindowRenderImplementation {
    fun enable(window: LWJGLWindow): RenderImplementationRenderer
    fun disable(window: LWJGLWindow, renderer: RenderImplementationRenderer)
}

interface RenderImplementationRenderer {
    fun render(window: LWJGLWindow, framebufferWidth: UInt, framebufferHeight: UInt)
}
