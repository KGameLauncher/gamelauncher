package de.dasbabypixel.gamelauncher.api.resource

import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture
import de.dasbabypixel.gamelauncher.api.util.stack.StackTrace

interface GameResource {
    val cleanedUp: Boolean
    val cleanupFuture: CompletableFuture<Unit>
    fun cleanupAsync(): CompletableFuture<Unit>

    interface StackCapable : GameResource {
        val creationStack: StackTrace?
        val creationThreadName: String?
        val cleanupStack: StackTrace?
        val cleanupThreadName: String?
    }
}
