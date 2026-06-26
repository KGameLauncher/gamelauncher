package de.dasbabypixel.gamelauncher.service

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class LoadedService<Service : Any>(val cls: KClass<Service>, val service: Service) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Service {
        return service
    }
}
