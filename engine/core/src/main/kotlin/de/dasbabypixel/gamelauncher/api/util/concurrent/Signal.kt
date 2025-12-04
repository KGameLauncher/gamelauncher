package de.dasbabypixel.gamelauncher.api.util.concurrent

import java.util.concurrent.locks.AbstractQueuedSynchronizer

class Signal : AbstractQueuedSynchronizer() {
    override fun tryAcquireShared(arg: Int): Int {
        return if (compareAndSetState(1, 0)) 1 else -1
    }

    override fun tryReleaseShared(arg: Int): Boolean {
        return compareAndSetState(0, 1)
    }

    fun await() {
        return acquireSharedInterruptibly(1)
    }

    fun signal() {
        releaseShared(1)
    }
}