package de.dasbabypixel.gamelauncher.api.util.concurrent

import java.util.concurrent.locks.AbstractQueuedSynchronizer

private class JSignal : AbstractQueuedSynchronizer(), Signal {
    override fun tryAcquireShared(arg: Int): Int {
        return if (compareAndSetState(1, 0)) 1 else -1
    }

    override fun tryReleaseShared(arg: Int): Boolean {
        return compareAndSetState(0, 1)
    }

    override fun await() {
        return acquireSharedInterruptibly(1)
    }

    override fun signal() {
        releaseShared(1)
    }
}

internal actual fun Signal.Companion.createSignal(): Signal {
    return JSignal()
}
