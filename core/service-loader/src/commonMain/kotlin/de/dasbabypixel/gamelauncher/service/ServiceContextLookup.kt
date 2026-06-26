package de.dasbabypixel.gamelauncher.service

class ServiceContextLookup(val identifier: String?) {
    fun accepts(service: RegisteredService<*>): Boolean {
        identifier?.let { if (service.context.identifier != it) return false }
        return true
    }

    companion object {
        val ANY = ServiceContextLookup(null)
    }
}
