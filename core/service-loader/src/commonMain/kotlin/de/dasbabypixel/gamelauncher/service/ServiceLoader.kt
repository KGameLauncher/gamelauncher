package de.dasbabypixel.gamelauncher.service

import kotlin.reflect.KClass

class ServiceLoader<Service : Any> internal constructor(
    val cls: KClass<*>, val services: List<RegisteredService<Service>>
) {
    val singleInstance: RegisteredService<Service>
        inline get() {
            if (services.isEmpty()) throw NoSuchElementException("No service registered for ${cls.simpleName}")
            if (services.size >= 2) throw IllegalStateException("Multiple services registered for ${cls.simpleName}")
            return services.single()
        }
}
