package de.dasbabypixel.gamelauncher.lwjgl.window

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glVertex2f
import org.lwjgl.opengl.GL46

interface WindowRenderImplementation {
    fun enable(window: LWJGLWindow): RenderImplementationRenderer
    fun disable(window: LWJGLWindow, renderer: RenderImplementationRenderer)
}

interface RenderImplementationRenderer {
    fun render(window: LWJGLWindow, framebufferWidth: Int, framebufferHeight: Int)
}

class DoubleBufferedAsyncRenderImpl : WindowRenderImplementation {
    override fun enable(window: LWJGLWindow): RenderImplementationRenderer {
        return DoubleBufferedAsyncRenderer(window)
    }

    override fun disable(window: LWJGLWindow, renderer: RenderImplementationRenderer) {
        renderer as DoubleBufferedAsyncRenderer
    }

    class DoubleBufferedAsyncRenderer(val window: LWJGLWindow) : RenderImplementationRenderer {
        override fun render(window: LWJGLWindow, framebufferWidth: Int, framebufferHeight: Int) {
//            println("Start render $framebufferWidth $framebufferHeight")
            GL11.glViewport(0, 0, framebufferWidth, framebufferHeight)

            val time = (System.currentTimeMillis() % 1000000).toFloat()
            GL46.glClear(GL46.GL_COLOR_BUFFER_BIT)
            GL46.glBegin(GL46.GL_TRIANGLES)
//        glVertex2f(sin(time / 900), cos(time / 2763F))
//        glVertex2f(cos(time / 1300 + 90), cos(time / 1050 + 10))
//        glVertex2f(sin(time / 1620 + 10), sin(time / 1000F))
            glVertex2f(-1F, -1F)
            glVertex2f(0F, 1F)
            glVertex2f(1F, 0F)
            GL46.glEnd()
//            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500))
        }
    }
}