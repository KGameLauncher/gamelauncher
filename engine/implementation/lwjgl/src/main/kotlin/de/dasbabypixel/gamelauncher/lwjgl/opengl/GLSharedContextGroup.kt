package de.dasbabypixel.gamelauncher.lwjgl.opengl

import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup
import de.dasbabypixel.gamelauncher.opengl.GLContextExecutor
import de.dasbabypixel.gamelauncher.opengl.GLSharedContextGroup
import java.util.concurrent.atomic.AtomicInteger

class LWJGLGLSharedContextGroup : GLSharedContextGroup() {
    companion object {
        private val counter = AtomicInteger(1)
    }

    private val group = ThreadGroup.create("GL-Main-Executors")

    override val mainContextExecutor: GLContextExecutor

    init {
        val handle = 0L
        val context = LWJGLGLContext(handle)
        mainContextExecutor = GLContextExecutor(group, "MainContextExecutor", context)
    }
}
