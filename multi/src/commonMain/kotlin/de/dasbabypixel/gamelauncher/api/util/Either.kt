package de.dasbabypixel.gamelauncher.api.util

class Either<F, S> {
    val first: F?
    val second: S?

    private constructor(first: F?, second: S?) {
        this.first = first
        this.second = second
    }

    companion object {
        fun <F, S> first(v: F): Either<F, S> = Either(v, null)
        fun <F, S> second(v: S): Either<F, S> = Either(null, v)
    }
}