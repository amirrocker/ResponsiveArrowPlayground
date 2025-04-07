package de.amirrocker.responsivearrowplayground

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import de.amirrocker.responsivearrowplayground.eventsourcing.GameId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

infix fun <A, B> Either<A, B>.shouldSucceedWith(expected: B) =
    assertEquals(expected.right(), this) {"expected ${expected.right()} but was $this"}

infix fun <A, B> Either<A, B>.shouldFailWith(expected: B) =
    assertEquals(expected.left(), this) {"expected ${expected.left()} but was $this"}

fun <T> Map<String, T>.dynamicTestsFor(block: (T)->Unit) =
    map {
        (message, example:T) -> DynamicTest.dynamicTest(message) {
            block(example)
        }
    }

// fixtures
fun anyGameId() = generateGameId()

@OptIn(ExperimentalUuidApi::class)
private fun generateGameId() = GameId(Uuid.random().toString())