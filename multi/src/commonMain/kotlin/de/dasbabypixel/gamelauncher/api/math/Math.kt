package de.dasbabypixel.gamelauncher.api.math

object Math {

}

fun Int.clamp(min: Int, max: Int) = if (this < min) min else if (this > max) max else this
