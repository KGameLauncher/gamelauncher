package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.impl.Providable

interface EfficientMPSC : Providable {
    fun <T : Any> create(instanceCreator: Function0<T>, size: Int): MPSC<T>
}

object EfficientMPSCs {
    fun <T : Any> create(instanceCreator: Function0<T>, size: Int): MPSC<T> {
        return instance.create(instanceCreator, size)
    }
}

internal expect val EfficientMPSCs.instance: EfficientMPSC

interface MPSC<T : Any> {
    fun <A, B, C> createPublisher(threeArgs: ThreeArgs<T, A, B, C>): PublisherThreeArgs<A, B, C>

    fun createPoller(handler: Handler<T>): Poller
}

fun interface Poller {
    fun poll()
}

fun interface Handler<T> {
    /**
     * @return continue
     */
    fun handle(event: T, endOfBatch: Boolean): Boolean
}

fun interface PublisherThreeArgs<A, B, C> {
    fun publish(a: A, b: B, c: C)
}

fun interface ThreeArgs<T, A, B, C> {
    fun update(event: T, a: A, b: B, c: C)
}