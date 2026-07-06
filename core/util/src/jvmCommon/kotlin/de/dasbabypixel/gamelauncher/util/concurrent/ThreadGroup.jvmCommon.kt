package de.dasbabypixel.gamelauncher.util.concurrent

import java.lang.Thread as JThread

private val rootThreadGroup = JThreadGroupCache[JThread.currentThread().threadGroup]

actual fun ThreadGroup.Companion.create(
    name: String, parent: ThreadGroup
): ThreadGroup {
    return JCommonThreadGroup(name, parent)
}

actual val ThreadGroup.Companion.root: de.dasbabypixel.gamelauncher.util.concurrent.ThreadGroup
    get() = rootThreadGroup
