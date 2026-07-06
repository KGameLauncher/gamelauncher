package de.dasbabypixel.gamelauncher.util.resource

import de.dasbabypixel.gamelauncher.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.util.stack.StackTrace

interface GameResource {
    val cleanedUp: Boolean
    val cleanupFuture: CompletableFuture<Unit>

    interface StackCapable : GameResource {
        val creationStack: StackTrace?
        val creationThreadName: String?
        val cleanupStack: StackTrace?
        val cleanupThreadName: String?
    }
}
