package de.dasbabypixel.gamelauncher.api.util.concurrent

import de.dasbabypixel.gamelauncher.impl.provider.Providable
import de.dasbabypixel.gamelauncher.impl.provider.provide

interface EfficientMPSC : Providable {
    fun <T : Any> create(instanceCreator: Function0<T>, size: Int): MPSC<T>
}

object EfficientMPSCs {
    private val instance = provide<EfficientMPSC>()

    fun <T : Any> create(instanceCreator: Function0<T>, size: Int): MPSC<T> {
        return instance.create(instanceCreator, size)
    }
}

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