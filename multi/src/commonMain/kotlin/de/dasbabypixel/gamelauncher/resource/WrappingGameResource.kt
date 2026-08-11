package de.dasbabypixel.gamelauncher.resource

import de.dasbabypixel.gamelauncher.api.util.concurrent.CompletableFuture

interface WrappingGameResource : GameResource {
    val wrapped: GameResource
    override val cleanedUp: Boolean
        get() = wrapped.cleanedUp
    override val cleanupFuture: CompletableFuture<Unit>
        get() = wrapped.cleanupFuture

    override fun cleanupAsync(): CompletableFuture<Unit> = wrapped.cleanupAsync()
}
