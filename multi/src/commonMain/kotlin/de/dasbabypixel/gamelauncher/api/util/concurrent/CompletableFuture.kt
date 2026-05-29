package de.dasbabypixel.gamelauncher.api.util.concurrent

class CompletableFuture<T> {
    var isDone: Boolean = false
        private set

    fun whenComplete(map: (T?, Throwable?) -> Unit) {
        TODO()
    }

    fun complete(value: T) {
        TODO("NYI")
    }

    fun completeExceptionally(t: Throwable) {
        TODO("NYI")
    }
}
