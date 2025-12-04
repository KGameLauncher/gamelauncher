package de.dasbabypixel.gamelauncher.opengl

import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractExecutorThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup

class GLContextExecutor(
    group: ThreadGroup, name: String, private val context: GLContext
) : AbstractExecutorThread(
    group, name, false
) {
    override fun startExecuting() {
        context.acquire()
    }

    override fun stopExecuting() {
        context.release()
    }
}