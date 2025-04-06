package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractExecutorThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.FrameSync
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL46
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SimpleRenderThread(
    group: ThreadGroup, override val window: LWJGLWindowImpl
) : AbstractExecutorThread(group, "${window.implName}RenderThread-${window.id}"), LWJGLRenderThread {
    private val frameSync = window.frameSync
    private val renderLock = ReentrantLock()
    private val framebufferLock = ReentrantLock()
    private var framebufferSize: ULong = 0uL
    private var preparedWidth: Int = 0
    private var preparedHeight: Int = 0
    private var renderer: RenderImplementationRenderer? = null
    private var renderingInstance: LWJGLWindowImpl.RenderingInstance? = null

    override fun startExecuting() {
        renderingInstance = window.startRendering()
        GL.createCapabilities()
        GL46.glClearColor(1F, 0F, 0F, 0.5F)
        singleRender()
    }

    fun framebufferUpdate(width: Int, height: Int) {
        val size = width.toUInt().toULong() shl 32 or height.toUInt().toULong()
        framebufferLock.withLock { framebufferSize = size }
        signal()
    }

    private fun prepareFramebufferSize() {
        val size = framebufferLock.withLock { framebufferSize }
        preparedWidth = (size shr 32).toInt()
        preparedHeight = size.toInt()
    }

    fun setRenderer(renderer: RenderImplementationRenderer?) {
        renderLock.lock()
        try {
            this.renderer = renderer
        } finally {
            renderLock.unlock()
        }
    }

    override fun preLoop() {
        frameSync.syncStart()
    }

    override fun postLoop() {
        frameSync.syncEnd()
    }

    override fun workExecution() {
        singleRender()
    }

    fun startNextFrame(frames: Int = 1): Long {
        return frameSync.startNextFrame(frames)
    }

    fun awaitFrame(frame: Long) {
        frameSync.waitForFrame(frame)
    }

    public override fun signal() {
        startNextFrame()
    }

    private fun singleRender() {
        println("render")
        prepareFramebufferSize()
        renderLock.withLock {
            renderer?.render(window, preparedWidth, preparedHeight)
            renderingInstance!!.swapBuffers()
        }
    }

    override fun stopExecuting() {
        GL.setCapabilities(null)
        renderingInstance!!.stopRendering()
        renderingInstance = null
    }
}