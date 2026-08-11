package de.dasbabypixel.gamelauncher.util.concurrent

interface ThreadGroup {
    val name: String
    val parent: ThreadGroup?

    companion object
}

expect fun ThreadGroup.Companion.create(
    name: String, parent: ThreadGroup = currentThread.group
): ThreadGroup

expect val ThreadGroup.Companion.root: ThreadGroup
