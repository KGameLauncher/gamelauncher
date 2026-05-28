package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractExecutorThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import java.util.concurrent.CompletableFuture
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SimpleRenderThread(
    group: ThreadGroup, override val window: LWJGLWindowImpl
) : AbstractExecutorThread(group, "${window.implName}RenderThread-${window.id}", customAwaitingSystem = true),
    LWJGLRenderThread {
    private val frameSync = window.frameSync
    private val renderLock = ReentrantLock()
    private val framebufferLock = ReentrantLock()
    private var framebufferSize: ULong = 0uL
    private var preparedWidth: UInt = 0u
    private var preparedHeight: UInt = 0u
    private var renderer: RenderImplementationRenderer? = null
    override val started: CompletableFuture<Unit> = CompletableFuture()

    override fun startExecuting() {
        window.startRendering()
        val size = window.framebufferSize
        sizeInternal(size.width, size.height)
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

    override fun customSignal() {
        startNextFrame()
    }

    override fun customAwaitWork() = error("Should not be called")

    private fun singleRender() {
        prepareFramebufferSize()
        val renderer = renderLock.withLock { renderer }

        renderer?.render(window, preparedWidth, preparedHeight)
    }

    override fun stopExecuting() {
    }
}