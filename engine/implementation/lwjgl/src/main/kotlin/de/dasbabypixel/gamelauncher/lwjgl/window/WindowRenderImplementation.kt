package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.opengl.GLProvider
import org.lwjgl.opengl.GL11.*
import java.util.concurrent.TimeUnit

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
        private var oldFBWidth = 0u
        private var oldFBHeight = 0u
//        private var lastTime = System.nanoTime()

        //        private var iter = 0
        override fun render(window: LWJGLWindow, framebufferWidth: UInt, framebufferHeight: UInt) {
            if (oldFBWidth != framebufferWidth || oldFBHeight != framebufferHeight) {
                oldFBWidth = framebufferWidth
                oldFBHeight = framebufferHeight
                glViewport(0, 0, framebufferWidth.toInt(), framebufferHeight.toInt())
            }
//            GLES.glClear(GLES20.GL_COLOR_BUFFER_BIT)
//            GLES.glViewport(0, 0, framebufferWidth, framebufferHeight)
            glClear(GL_COLOR_BUFFER_BIT)


//            val time = (System.currentTimeMillis() % 1000000).toFloat()
//            iter += 10
//            val time = (iter).toFloat()
//            GLES.glClear(GLES20.GL_COLOR_BUFFER_BIT)
//            GL46.glColor4f(1F, 0F, 0F, 0.5F)
//            GL46.glBegin(GL46.GL_QUADS)
//            GL46.glVertex2f(-1F, -1F)
//            GL46.glVertex2f(-1F, 1F)
//            GL46.glVertex2f(1F, 1F)
//            GL46.glVertex2f(1F, -1F)
//            GL46.glEnd()

//            GL46.glColor4f(1F, 1F, 1F, 1F)
//            GL46.glBegin(GL46.GL_TRIANGLES)
//            GL46.glVertex2f(sin(time / 900), cos(time / 2763F))
//            GL46.glVertex2f(cos(time / 1300 + 90), cos(time / 1050 + 10))
//            GL46.glVertex2f(sin(time / 1620 + 10), sin(time / 1000F))
//            GL46.glVertex2f(-1F, -1F)
//            GL46.glVertex2f(0F, 1F)
//            GL46.glVertex2f(1F, 0F)
//            GL46.glEnd()

//            Thread.sleep(100)

            val revolve = TimeUnit.SECONDS.toNanos(5)
            val thisTime = System.nanoTime()
            val time = thisTime % revolve
            val percent = time.toDouble() / revolve.toDouble()
//            val elapsed: Float = (lastTime - thisTime) / 1E9f
//            lastTime = thisTime

            val aspect: Float = framebufferWidth.toFloat() / framebufferHeight.toFloat()
            glMatrixMode(GL_PROJECTION)
            glLoadIdentity()
            glOrtho(-1.0 * aspect, +1.0 * aspect, -1.0, +1.0, -1.0, +1.0)

            glMatrixMode(GL_MODELVIEW)
            glLoadIdentity()
//            glRotatef(elapsed * 10.0f, 0F, 0F, 1F)
            glRotated(percent * 360, 0.0, 0.0, 1.0)
            glBegin(GL_QUADS)
            glVertex2f(-0.5f, -0.5f)
            glVertex2f(+0.5f, -0.5f)
            glVertex2f(+0.5f, +0.5f)
            glVertex2f(-0.5f, +0.5f)
            glEnd()

//            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100))
        }
    }
}