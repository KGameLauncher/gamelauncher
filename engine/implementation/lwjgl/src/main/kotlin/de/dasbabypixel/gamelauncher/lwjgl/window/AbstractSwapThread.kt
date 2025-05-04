package de.dasbabypixel.gamelauncher.lwjgl.window

import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractExecutorThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.ThreadGroup

abstract class AbstractSwapThread(
    group: ThreadGroup, val window: LWJGLWindowImpl
) : AbstractExecutorThread(
    group, "${window.implName}RenderThread-${window.id}"
) {

}