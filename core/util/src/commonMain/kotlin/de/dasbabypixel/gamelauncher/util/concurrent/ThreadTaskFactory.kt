package de.dasbabypixel.gamelauncher.util.concurrent

interface ThreadTaskFactory {
    fun createTask(thread: Thread): ThreadTask
}
