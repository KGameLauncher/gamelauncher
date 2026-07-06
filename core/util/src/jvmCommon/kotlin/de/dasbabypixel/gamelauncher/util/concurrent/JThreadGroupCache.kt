package de.dasbabypixel.gamelauncher.util.concurrent

import java.util.WeakHashMap

import java.lang.ThreadGroup as JThreadGroup

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
