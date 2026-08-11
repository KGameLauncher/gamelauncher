package de.dasbabypixel.gamelauncher.util.extension

fun Boolean.Companion.getBoolean(name: String): Boolean {
    return java.lang.Boolean.getBoolean(name)
}