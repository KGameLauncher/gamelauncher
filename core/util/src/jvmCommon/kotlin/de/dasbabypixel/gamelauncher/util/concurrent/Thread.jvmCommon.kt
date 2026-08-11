package de.dasbabypixel.gamelauncher.util.concurrent

import java.util.concurrent.locks.LockSupport
import java.lang.Thread as JThread
import java.lang.ThreadGroup as JThreadGroup

actual fun Thread.Companion.park() = LockSupport.park()
actual fun Thread.Companion.park(nanos: Long) = LockSupport.park(nanos)
actual fun Thread.Companion.sleep(millis: Long) = JThread.sleep(millis)

@Suppress("NOTHING_TO_INLINE") // We inline in order to have a nicer dump
actual inline fun Thread.Companion.dumpStack() = JThread.dumpStack()

actual fun Thread.Companion.create(
    name: String, taskFactory: ThreadTaskFactory, daemon: Boolean, group: ThreadGroup
): Thread {
    return JThreadImpl(name, JThreadGroupCache[group], taskFactory, daemon).thread
}

private class JThreadImpl(
    name: String, group: JThreadGroup, taskFactory: ThreadTaskFactory, daemon: Boolean
) : JThread(group, name), ThreadHolder {
    override val thread: ThreadImpl = ThreadImpl(this, taskFactory)

    override fun run() {
        thread.task.run()
    }

    init {
        if (daemon) isDaemon = true
    }
}