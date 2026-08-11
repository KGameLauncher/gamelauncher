package de.dasbabypixel.gamelauncher.util.concurrent

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
