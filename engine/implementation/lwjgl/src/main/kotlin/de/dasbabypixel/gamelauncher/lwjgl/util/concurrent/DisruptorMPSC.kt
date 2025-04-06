package de.dasbabypixel.gamelauncher.lwjgl.util.concurrent

import com.lmax.disruptor.EventPoller
import com.lmax.disruptor.EventTranslatorThreeArg
import com.lmax.disruptor.RingBuffer
import com.lmax.disruptor.SleepingWaitStrategy
import de.dasbabypixel.gamelauncher.api.util.concurrent.*
import de.dasbabypixel.gamelauncher.impl.provider.registerProvider

object DisruptorMPSC : EfficientMPSC {
    init {
        registerProvider<EfficientMPSC>("disruptor_mpsc")
    }

    override fun <T : Any> create(instanceCreator: () -> T, size: Int): MPSC<T> {
        return object : MPSC<T> {
            val buffer: RingBuffer<T> = RingBuffer.createMultiProducer(instanceCreator, size, SleepingWaitStrategy())

            override fun <A, B, C> createPublisher(threeArgs: ThreeArgs<T, A, B, C>): PublisherThreeArgs<A, B, C> {
                val handler = EventTranslatorThreeArg<T, A, B, C> { event, _, arg0, arg1, arg2 ->
                    threeArgs.update(event, arg0, arg1, arg2)
                }
                return PublisherThreeArgs { a, b, c ->
                    buffer.publishEvent(handler, a, b, c)
                }
            }

            override fun createPoller(handler: Handler<T>): Poller {
                val poller = buffer.newPoller()
                buffer.addGatingSequences(poller.sequence)
                val h = EventPoller.Handler<T> { event, _, endOfBatch -> handler.handle(event, endOfBatch) }
                return Poller { poller.poll(h) }
            }
        }
    }
}