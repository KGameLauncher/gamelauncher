package de.dasbabypixel.gamelauncher.api

import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import de.dasbabypixel.gamelauncher.impl.provider.Providable
import de.dasbabypixel.gamelauncher.impl.provider.provide
import java.util.concurrent.atomic.AtomicReference
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.system.exitProcess

abstract class GameLauncher : Providable {
    companion object {
        private val logger = getLogger<GameLauncher>()
        private val launcher: AtomicReference<GameLauncher?> = AtomicReference(null)

        fun start() {
            val provided = provide<GameLauncher>()
            if (launcher.compareAndSet(null, provided)) {
                provided.start()
            } else throw IllegalStateException("Already running")
        }

        fun stop() {
            launcher.get()!!.stop()
        }

        fun handleException(ex: Throwable) {
            try {
                launcher.get()!!.handleException(ex)
            } catch (t: Throwable) {
                logger.error("Failed to handle exception and properly shut down", t)
                exitProcess(0)
            }
        }
    }

    abstract fun start()
    abstract fun stop()
    abstract fun handleException(ex: Throwable)
}

@OptIn(ExperimentalContracts::class)
inline fun launcherHandlesException(function: () -> Unit) {
    contract { callsInPlace(function, InvocationKind.EXACTLY_ONCE) }
    try {
        function()
    } catch (t: Throwable) {
        GameLauncher.handleException(t)
    }
}