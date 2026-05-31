package de.dasbabypixel.gamelauncher.impl

import Test2
import de.dasbabypixel.gamelauncher.api.util.concurrent.Thread
import de.dasbabypixel.gamelauncher.api.util.concurrent.configureThirdPartyThread
import de.dasbabypixel.gamelauncher.api.util.concurrent.currentThread
import de.dasbabypixel.gamelauncher.api.util.logging.getLogger
import java.lang.Thread as JThread

fun main() {
    JThread.currentThread().configureThirdPartyThread()
    println("ABC")
    println(Thread.currentThread.name)
    println(Thread.currentThread.group)
    getLogger<Test2>().warn("test")
    getLogger<Test2>().warn("test")
    getLogger<Test2>().error("test")

    println("ABC")
}
