package de.dasbabypixel.gamelauncher.util.resource

import java.util.concurrent.CompletableFuture

interface GameResource {
    val cleanedUp: Boolean
    val cleanupFuture: CompletableFuture<Unit>
    fun cleanupAsync(): CompletableFuture<Unit>

    interface StackCapable : GameResource {
        val creationStack: Array<StackTraceElement>?
        val creationThreadName: String?
        val cleanupStack: Array<StackTraceElement>?
        val cleanupThreadName: String?
    }
}
