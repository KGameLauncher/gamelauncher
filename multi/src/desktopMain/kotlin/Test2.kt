import java.lang.foreign.Arena

class Test2 {
    fun abc() {
        Arena.ofAuto()
        Test().abc()
    }

    companion object
}

fun main() {
    println(Test2)
}
