package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.gles.GLES20
import de.dasbabypixel.gamelauncher.opengl.GLProvider
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.LockSupport

interface WindowRenderImplementation {
    fun enable(window: LWJGLWindow): RenderImplementationRenderer
    fun disable(window: LWJGLWindow, renderer: RenderImplementationRenderer)
}

interface RenderImplementationRenderer {
    fun render(window: LWJGLWindow, framebufferWidth: UInt, framebufferHeight: UInt)
}

class DoubleBufferedAsyncRenderImpl : WindowRenderImplementation {

    override fun enable(window: LWJGLWindow): RenderImplementationRenderer {
        return DoubleBufferedAsyncRenderer(window)
    }

    override fun disable(window: LWJGLWindow, renderer: RenderImplementationRenderer) {
        renderer as DoubleBufferedAsyncRenderer
    }

    class DoubleBufferedAsyncRenderer(val window: LWJGLWindow) : RenderImplementationRenderer {
        private val GLES = GLProvider.GLES
        override fun render(window: LWJGLWindow, framebufferWidth: UInt, framebufferHeight: UInt) {
//            println("Start render $framebufferWidth $framebufferHeight")
            println("render")
            GLES.glViewport(0, 0, framebufferWidth, framebufferHeight)

            val time = (System.currentTimeMillis() % 1000000).toFloat()
            GLES.glClear(GLES20.GL_COLOR_BUFFER_BIT)
//            GLES.glColor4f(1F, 0F, 0F, 0.5F)
//            GLES.glBegin(GL46.GL_QUADS)
//            glVertex2f(-1F, -1F)
//            glVertex2f(-1F, 1F)
//            glVertex2f(1F, 1F)
//            glVertex2f(1F, -1F)
//            GLES.glEnd()
//
//            GLES.glColor4f(1F, 1F, 1F, 1F)
//            GLES.glBegin(GL46.GL_TRIANGLES)
//        glVertex2f(sin(time / 900), cos(time / 2763F))
//        glVertex2f(cos(time / 1300 + 90), cos(time / 1050 + 10))
//        glVertex2f(sin(time / 1620 + 10), sin(time / 1000F))
//            glVertex2f(-1F, -1F)
//            glVertex2f(0F, 1F)
//            glVertex2f(1F, 0F)
//            GLES.glEnd()
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500))
        }
    }
}