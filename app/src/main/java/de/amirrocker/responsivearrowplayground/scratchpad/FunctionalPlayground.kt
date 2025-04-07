package de.amirrocker.responsivearrowplayground.scratchpad

val multiplyAsFunctionReference: (Int)->Int = ::double

fun testSimpleLambda() {
    val resultMultiply = multiplyAsFunctionReference(6)
    assert(resultMultiply == 12) {"expect 12 but was $resultMultiply"}.also {
        println("resultMultiply is $resultMultiply")
    }

    val resultTriple = multiplyAsFunctionReference(triple(4))
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

// lets start by playing around with lambdas
fun double(n: Int): Int = n * 2

val multiply: (Int) -> Int = { n: Int -> double(n) }

// simply use a reference on this 'script'
val multiply2: (Int) -> Int = ::double

// or on a class
data class RefTest(val value: Int) {
    fun double(n: Int): Int = n * 2
}

val foo = RefTest(42)
val multiply3: (Int) -> Int = foo::double

// Boring. We all know that :)
// Lets do something more fun. Something like
// Composing functions

fun square(n: Int): Int = n * n
fun triple(n: Int): Int = n * 3
// well, it works, but not quite adequate.

// try again...
// this is a more flexible approach since we can do the actual calculations inside the lambda
// instead of having them hardcoded like above.
fun composedSquare(fn: (Int) -> Int): (Int) -> Int = { x -> fn(square(x)) }
fun composedTriple(fn: (Int) -> Int): (Int) -> Int = { x -> fn(triple(x)) }

// this is the "base" lambda that accepts the different "calculation" lambdas
fun composed(f: (Int) -> Int, g: (Int) -> Int): (Int) -> Int = { x -> f(g(x)) }

// now we can compose the functions like this
fun tester() = println(
    "tester1: ${composed(::double, ::triple)}"
)

fun tester2() = println(
    "tester2: ${composed(composedSquare { x -> x * x }, ::triple)}"
)

fun tester3() = println(
    "tester3: ${composed(composedSquare { x -> x * x * x }, composedTriple { x -> x * x * x })}"
)

// awesome! Lets improve on them functions a bit more :P
// lets make them polymorphic
fun <T : Number> polySquare(f: (T) -> T): (T) -> T = { t -> f(t) }

// different types used
fun squareInt(n: Int): Int = n * n
fun squareDouble(n: Double): Double = n * n
fun squareIntToDouble(n: Int): Double = n.toDouble() * n.toDouble()

// now use the polymorphic tester with the different types.
fun polyTesterWithInt() = polySquare<Int> { x -> squareInt(x) }
fun polyTesterWithDouble() = polySquare<Double> { x -> squareDouble(x) }

// But, it still not a really good solution. We can only use a single type across the function.
// to make it even more flexible consider this code:
fun <U : Number, V : Number, W : Number> polymorphicCalculator(
    f: (V) -> W, g: (U) -> V
): (U) -> W = { x ->
    f(
        g(x)
    )
}

fun runPolymorphicCalculatorTest() {

    // lets see
    val result = polymorphicCalculator<Double, Int, Double>(
        {
            println("inside f: calculate square of $it")
            val result = squareIntToDouble(it)
            println("result of f: $result")
            result
        },
        {
            println("inside g: calculate square of $it")
            val result = squareDouble(it)
            println("result of g: $result")
            result.toInt()
        }
    )
    println("final outer result: ${result(10.0)}")
}

// reusing arguments
val tupleAdding: (Int) -> (Int) -> Int = { a -> { b -> a + b } }

// test it
fun testTupleAdding() = println("triplet: ${tupleAdding(3)(4)}")

// now do the same drill, make them polymorphic
val polyTuple: (T: Number) -> (U: Number) -> (Double) = { a: Number -> { b: Number -> a.toDouble() + b.toDouble() } }

// test it again
fun <U : Number, V : Number> testPolyTuple(a: U, b: V) = println("polyTuple: ${polyTuple(a)(b)}")

// looks ok but results made clear that parameter names play a crucial role when trying to understand
// the different inputs and their resulting output.
// These functions are said to be the curried form of the equivalent functions of tuples.
// basically fun getTwoValues():Tuple / Pair / Triple ....
val calculate = fun(f: (Int) -> Int, g: (Int) -> Int): (Int) -> Int = { x -> f(g(x)) }

//typealias T = ((Int) -> Int)

//val   calculateFlex: ((Int) -> Int) -> ((Int) -> Int) -> (Int) -> Int = {
val calculateFlex: ((Int) -> Int) -> ((Int) -> Int) -> (Int) -> Int = { x/*:(Int) -> Int*/ ->
    { y/*:(Int) -> Int*/ ->
        { z: Int ->
            println("inside : z: $z")
            val result = x(y(z))
            println("final result: $result")
            result
        }
    }
}

// I am unable to make the top property use T instead of Int

fun testDifferentCalculationLambdas() {

    // we calculate square and triple =>
    val resultManuallyCalculated = calculate(
        { x -> x * x },
        { x -> x * x * x }
    )
    println("resultManuallyCalculated: ${resultManuallyCalculated(10)}")

    val resultByFunctionReference = calculate(
        ::square,
        ::triple
    )
    println("resultByFunctionReference: ${resultByFunctionReference(10)}")

    val resultOfCalculateFlex = calculateFlex { x: Int -> x * x }() { y -> y * y }
    println("resultOfCalculateFlex: ${resultOfCalculateFlex(5)}")

    val resultOfCalculateFlexWithFunctionReferences = calculateFlex(::square)(::triple)
    println("resultOfCalculateFlex: ${resultOfCalculateFlexWithFunctionReferences(5)}")

}