package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractExecutorThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import de.dasbabypixel.gamelauncher.opengl.GLProvider
import org.lwjgl.opengl.GL
import org.lwjgl.opengles.GLES
import java.util.concurrent.CompletableFuture
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SimpleRenderThread(
    group: ThreadGroup, override val window: LWJGLWindowImpl
) : AbstractExecutorThread(
    group, "${window.implName}RenderThread-${window.id}"
), LWJGLRenderThread {
    private val frameSync = window.frameSync
    private val renderLock = ReentrantLock()
    private val framebufferLock = ReentrantLock()
    private var framebufferSize: ULong = 0uL
    private var preparedWidth: UInt = 0u
    private var preparedHeight: UInt = 0u
    private var renderer: RenderImplementationRenderer? = null
    private var renderingInstance: LWJGLWindowImpl.RenderingInstance? = null
    private val gl = GLProvider.GLES
    override val started: CompletableFuture<Unit> = CompletableFuture()

    override fun startExecuting() {
        Thread.sleep(400)
        renderingInstance = window.startRendering()
        val size = window.framebufferSize
        GLES.createCapabilities()
        sizeInternal(size.width, size.height)
        gl.glClearColor(0F, 0F, 0F, 0F)
        started.complete(Unit)
    }

    private fun sizeInternal(width: Int, height: Int) {
        val size = width.toUInt().toULong() shl 32 or height.toUInt().toULong()
        framebufferLock.withLock { framebufferSize = size }
    }

    fun framebufferUpdate(width: Int, height: Int) {
        sizeInternal(width, height)
//        signal()
        val frame = startNextFrame()
        awaitFrame(frame)
    }

    private fun prepareFramebufferSize() {
        val size = framebufferLock.withLock { framebufferSize }
        preparedWidth = (size shr 32).toUInt()
        preparedHeight = size.toUInt()
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

    override fun customSignal(): Boolean {
        startNextFrame()
        return true
    }

    private fun singleRender() {
        prepareFramebufferSize()
        renderLock.withLock {
            renderer?.render(window, preparedWidth, preparedHeight)
            renderingInstance!!.swapBuffers()
            println("swap")
        }
    }

    override fun stopExecuting() {
        GL.setCapabilities(null)
        renderingInstance!!.stopRendering()
        renderingInstance = null
    }
}