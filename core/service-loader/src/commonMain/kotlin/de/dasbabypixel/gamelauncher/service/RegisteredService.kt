package de.dasbabypixel.gamelauncher.service

import kotlin.reflect.KClass

class RegisteredService<Service : Any> internal constructor(
    val context: ServiceContext, val cls: KClass<Service>, internal val loadFunction: () -> Service
) {
    fun load(): LoadedService<Service> {
        return LoadedService(cls, loadFunction())
    }
}
