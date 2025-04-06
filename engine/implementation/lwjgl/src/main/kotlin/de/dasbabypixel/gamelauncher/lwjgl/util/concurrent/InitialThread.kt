package de.dasbabypixel.gamelauncher.lwjgl.util.concurrent

import de.dasbabypixel.gamelauncher.api.util.concurrent.AbstractExecutorThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.Executor
import de.dasbabypixel.gamelauncher.api.util.concurrent.ExecutorThread
import de.dasbabypixel.gamelauncher.api.util.function.GameConsumer
import de.dasbabypixel.gamelauncher.impl.util.concurrent.CommonThreadHelper
import java.lang.Thread
import java.util.function.BooleanSupplier
import java.lang.Thread as JThread

class InitialThread private constructor(
    private val implementation: Implementation, private val runningCallback: Runnable
) : AbstractExecutorThread(CommonThreadHelper.initialThread) {
    companion object {
        fun init(implementation: Implementation, consumer: GameConsumer<ExecutorThread>, runningCallback: Runnable) {
            val initial = CommonThreadHelper.initialThread
            val current = JThread.currentThread()
            if (initial !== current) error("InitialThread != currentThread")
            val thread = InitialThread(implementation, runningCallback)
            val handle = implementation.initialized(thread)
            CommonThreadHelper.mapInitialThread(handle)
            thread.track()
            consumer.accept(handle)
            thread.run0()
        }

        fun cleanup() {
            CommonThreadHelper.mappedInitialThread.cleanup().join()
        }
    }

    init {
        threadImpl.name = implementation.name
    }

    override fun startExecuting() {
        implementation.startExecuting()
        runningCallback.run()
    }

    override fun workExecution() {
        implementation.workExecution()
    }

    override fun stopExecuting() {
        implementation.stopExecuting()
    }

    override fun signal() {
        implementation.signal { super.signal() }
    }

    override fun awaitWork(): Boolean {
        return implementation.awaitWork { super.awaitWork() }
    }

    override fun customSignal(): Boolean {
        return implementation.customSignal()
    }

    override fun customAwait(): Boolean {
        return implementation.customAwait()
    }

    override fun customAwaitWork() {
        return implementation.customAwaitWork()
    }

    override fun waitForSignal() {
        implementation.waitForSignal { super.waitForSignal() }
    }

    override val customStart: Boolean
        get() = true
    override val autoTrack: Boolean
        get() = false

    override fun customStart() = error("Can't start initial thread")

    interface Implementation {
        val name: String

        fun initialized(thread: ExecutorThread): ExecutorThread

        fun startExecuting()

        fun workExecution()

        fun stopExecuting()

        fun signal(superSignal: Runnable)

        fun customSignal(): Boolean

        fun waitForSignal(superWait: Runnable)

        fun customAwait(): Boolean

        fun awaitWork(superAwaitWork: BooleanSupplier): Boolean

        fun customAwaitWork()
    }
}