package de.amirrocker.responsivearrowplayground.scratchpad

fun double(a:Int) = a * 2
fun triple(a:Int) = a * 3
val multiply: (Int)->Int = ::double

fun testSimpleLambda() {
    val resultMultiply = multiply(6)
    assert(resultMultiply == 12) {"expect 12 but was $resultMultiply"}.also {
        println("resultMultiply is $resultMultiply")
    }

    val resultTriple = multiply(triple(4))
    assert(resultTriple == 24) {"expect 24 but was $resultTriple"}.also {
        println("resultTriple is $resultTriple")
    }
}

val composeMultiply: (Int)->(Int)->Int = { x ->
    { y ->
        x * y
    }
}

val composeMultiplyTriple: (Int)->(Int)->(Int)->Int = { x ->
    { y ->
        { z ->
            x * y * z
        }
    }
}

fun testComposeMultiply() {
    val result = composeMultiply(4)(3)
    assert(result == 12) {"expect 12 but was $result"}.also {
        println("resultComposeMultiply is $result")
    }

    val double: (Int)->Int = { x -> x * 2 }
    val powerOfTwo: (Int)->Int = { x -> x * x }
    val powerOfThree: (Int)->Int = { x -> x * x * x }
    // the power of maaaannnyyyyy :D

    val resultMultiply = composeMultiply(double(3))(powerOfTwo(3))
    assert(resultMultiply == 54) {"expect 54 but was $resultMultiply"}.also {
        println("resultMultiply is $resultMultiply")
    }

    val resultMultiplyTriple = composeMultiplyTriple(double(3))(powerOfTwo(3))(powerOfThree(3))
    assert(resultMultiplyTriple == 54*27) {"expect 54*27 but was $resultMultiplyTriple"}.also {
        println("resultMultiplyTriple is $resultMultiplyTriple")
    }
}



