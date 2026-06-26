package de.dasbabypixel.gamelauncher.logging

class PatternException(text: String) : RuntimeException(text) {
    constructor(e: PatternException, suffix: String) : this(e.message + suffix) {
        this@PatternException.stackTrace = e.stackTrace
    }
}
