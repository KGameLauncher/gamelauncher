package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.api.util.buildStackTrace
import de.dasbabypixel.gamelauncher.api.util.stack.StackTrace
import java.util.WeakHashMap
import java.util.concurrent.locks.LockSupport
import java.lang.Thread as JThread
import java.lang.ThreadGroup as JThreadGroup

actual fun ThreadGroup.Companion.create(
    name: String, parent: ThreadGroup
): ThreadGroup {
    return JCommonThreadGroup(name, parent)
}

internal class JCommonThreadGroup : ThreadGroup {
    override val name: String
    override val parent: ThreadGroup?
    val group: JThreadGroup

    constructor(group: JThreadGroup) {
        this.name = group.name
        this.parent = group.parent?.let { JThreadGroupCache[it] }
        this.group = group
    }

    constructor(name: String) : this(name, JThreadGroupCache[JThread.currentThread().threadGroup])

    constructor(name: String, parent: ThreadGroup) {
        this.name = name
        this.parent = parent
        this.group = JThreadGroup(JThreadGroupCache[parent], name)
    }

    override fun toString(): String {
        return name
    }
}

internal interface ThreadHolder {
    val thread: Thread
}

internal object JThreadGroupCache {
    private val map = WeakHashMap<JThreadGroup, ThreadGroup>()
    operator fun get(group: JThreadGroup): ThreadGroup {
        synchronized(map) {
            return map.computeIfAbsent(group) { JCommonThreadGroup(it) }
        }
    }

    operator fun get(group: ThreadGroup): JThreadGroup {
        return (group as JCommonThreadGroup).group
    }
}

internal class ThreadImpl(val thread: JThread, threadTaskFactory: ThreadTaskFactory) : Thread {
    override val name: String
        get() = thread.name
    override val group: ThreadGroup = JThreadGroupCache[thread.threadGroup]
    override val stacktrace: StackTrace
        get() = buildStackTrace(thread.stackTrace)
    override val task = threadTaskFactory.createTask(this)

    override fun start() {
        thread.start()
    }

    override fun unpark() {
        LockSupport.unpark(thread)
    }

    override val cleanedUp: Boolean
        get() = task.cleanedUp
    override val cleanupFuture: CompletableFuture<Unit>
        get() = task.cleanupFuture

    override fun cleanupAsync(): CompletableFuture<Unit> = task.cleanupAsync()
}

actual fun Thread.Companion.park() = LockSupport.park()
actual fun Thread.Companion.park(nanos: Long) = LockSupport.park(nanos)
actual fun Thread.Companion.sleep(millis: Long) = JThread.sleep(millis)

@Suppress("NOTHING_TO_INLINE") // We inline in order to have a nicer dump
actual inline fun Thread.Companion.dumpStack() = JThread.dumpStack()
