package de.dasbabypixel.gamelauncher.impl.util.concurrent

import de.dasbabypixel.gamelauncher.api.util.concurrent.*
import de.dasbabypixel.gamelauncher.impl.provider.registerProvider
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.locks.LockSupport
import javax.swing.JTree

import java.lang.Thread as JThread

class CommonThreadGroup : ThreadGroup.Opened {
    override val name: String
    override val parent: ThreadGroup?
    val group: java.lang.ThreadGroup

    constructor(group: java.lang.ThreadGroup) {
        this.name = group.name
        this.parent = group.parent?.let { ThreadGroupCache[it] }
        this.group = group
    }

    constructor(name: String) : this(name, ThreadGroupCache[JThread.currentThread().threadGroup])

    constructor(name: String, parent: ThreadGroup) {
        this.name = name
        this.parent = parent
        this.group = ThreadGroup(ThreadGroupCache[parent], name)
    }
}

object ThreadGroupCache : InternalThreadGroupCache {
    init {
        registerProvider<InternalThreadGroupCache>("thread_group_cache")
    }

    private val map = WeakHashMap<java.lang.ThreadGroup, ThreadGroup>()
    override operator fun get(group: java.lang.ThreadGroup): ThreadGroup {
        synchronized(map) {
            return map.computeIfAbsent(
                group, ::CommonThreadGroup
            )
        }
    }

    override operator fun get(group: ThreadGroup): java.lang.ThreadGroup {
        return (group as CommonThreadGroup).group
    }
}

object CommonThreadHelper : ThreadsProvider {
    val initialThread: JThread = JThread.currentThread()
    lateinit var mappedInitialThread: Thread
        private set

    init {
        ThreadGroupCache // init group cache
        registerProvider<ThreadsProvider>("threads_provider")
    }

    fun mapInitialThread(thread: Thread) {
        this.mappedInitialThread = thread
    }

    override fun currentThread(): Thread {
        val thread = JThread.currentThread()
        if (thread === initialThread) return mappedInitialThread
        if (thread !is ThreadHolder) throw IllegalStateException("Current thread $thread is not a ThreadHolder")
        return thread.thread
    }

    override fun park() {
        LockSupport.park()
    }

    override fun park(nanos: Long) {
        LockSupport.parkNanos(nanos)
    }

    override fun sleep(millis: Long) {
        JThread.sleep(millis)
    }

    override fun createGroup(name: String): ThreadGroup {
        return CommonThreadGroup(name)
    }

    override fun createGroup(name: String, parent: ThreadGroup): ThreadGroup {
        return CommonThreadGroup(name, parent)
    }
}
