import de.dasbabypixel.gamelauncher.service.ServiceRegistry

fun main() {
    println("ABC")

    ServiceRegistry.global.register("abc") {
        "test"
    }

    ServiceRegistry.global.register("def") {
        "test2"
    }

    val str: String by ServiceRegistry.global.lookupIdentifier("abc")

    println(str)


}
