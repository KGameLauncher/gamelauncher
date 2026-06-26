package de.dasbabypixel.gamelauncher.service

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class ServiceRegistry private constructor(
    private val allowMutableRead: Boolean
) : ServiceRegistryLookup(ServiceContextLookup.ANY) {

    constructor() : this(false)

    private val servicesByCls: MutableMap<KClass<out Any>, MutableList<RegisteredService<out Any>>> =
        HashMap()
    private var frozen = false

    fun <Service : Any> register(
        context: ServiceContext, cls: KClass<Service>, loadFunction: () -> Service
    ): RegisteredService<Service> {
        if (frozen) error("Frozen")
        val registered = RegisteredService(context, cls, loadFunction)
        servicesByCls.getOrPut(cls, ::ArrayList).add(registered)
        return registered
    }

    inline fun <reified Service : Any> register(noinline loadFunction: () -> Service) =
        register(ServiceContext("default"), Service::class, loadFunction)

    inline fun <reified Service : Any> register(
        identifier: String, noinline loadFunction: () -> Service
    ) = register(ServiceContext(identifier), Service::class, loadFunction)

    fun freeze() {
        if (frozen) error("Frozen")
        frozen = true
    }

    fun lookup(lookup: ServiceContextLookup): ServiceRegistryLookup = SpecificLookup(lookup)

    fun lookupIdentifier(identifier: String): ServiceRegistryLookup =
        SpecificLookup(ServiceContextLookup(identifier))

    override fun <Service : Any> loaderFor(cls: KClass<Service>): ServiceLoader<Service> =
        loaderFor(cls, lookup)

    fun <Service : Any> loaderFor(
        cls: KClass<Service>, lookup: ServiceContextLookup
    ): ServiceLoader<Service> {
        if (!allowMutableRead && !frozen) error("Not frozen")
        return servicesByCls[cls]?.let { it as List<RegisteredService<Service>> }.let {
            it ?: emptyList()
        }.let {
            it.filter { service -> lookup.accepts(service) }
        }.let {
            ServiceLoader(cls, it)
        }
    }

    private inner class SpecificLookup(lookup: ServiceContextLookup) :
        ServiceRegistryLookup(lookup) {

        override fun <Service : Any> loaderFor(cls: KClass<Service>): ServiceLoader<Service> =
            loaderFor(cls, lookup)
    }

    companion object {
        val global = ServiceRegistry(true)
    }
}

sealed class ServiceRegistryLookup(val lookup: ServiceContextLookup) {
    abstract fun <Service : Any> loaderFor(cls: KClass<Service>): ServiceLoader<Service>

    inline fun <reified Service : Any> singleInstance(): Service {
        return loaderFor(Service::class).singleInstance.load().service
    }

    inline operator fun <reified Service : Any> provideDelegate(
        thisRef: Any?, property: KProperty<*>
    ): LoadedService<Service> {
        return loaderFor(Service::class).singleInstance.load()
    }
}
