package de.dasbabypixel.gamelauncher.api.util.logging

object JvmLogging {
    /**
     * The raw InputStream. You should probably never use this
     */
    val `in` = System.`in`!!

    /**
     * The raw stdout. You should probably never use this, instead use proper logging
     */
    val out = System.out!!

    /**
     * The raw stderr. You should probably never use this, instead use proper logging
     */
    val err = System.err!!

    fun init() {}
}
