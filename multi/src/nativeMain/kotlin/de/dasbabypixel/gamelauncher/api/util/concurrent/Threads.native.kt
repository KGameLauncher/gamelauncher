package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.api.util.stack.StackTrace

internal actual val NativeThreads.currentThread: NativeThread
    get() = TODO("Not yet implemented")

internal actual fun NativeThreads.unpark(thread: NativeThread) {
}

internal actual var NativeThread.name: String
    get() = TODO("Not yet implemented")
    set(value) {}
internal actual val NativeThread.stackTrace: StackTrace
    get() = TODO("Not yet implemented")
internal actual var NativeThread.isDaemon: Boolean
    get() = TODO("Not yet implemented")
    set(value) {}
internal actual val NativeThread.threadGroup: NativeThreadGroup
    get() = TODO("Not yet implemented")

internal actual fun NativeThread.start() {
}

internal actual class NativeThreadGroup

internal actual open class NativeThread {
    actual constructor(
        threadGroup: NativeThreadGroup,
        task: NativeThreadTask
    ) {
        TODO("Not yet implemented")
    }

    actual constructor(
        threadGroup: NativeThreadGroup,
        task: NativeThreadTask,
        name: String
    ) {
        TODO("Not yet implemented")
    }
}

internal actual val provider: ThreadsProvider
    get() = TODO("Not yet implemented")
internal actual val ThreadGroupCache: InternalThreadGroupCache
    get() = TODO("Not yet implemented")

internal actual fun interface NativeThreadTask {
    actual fun run()
}