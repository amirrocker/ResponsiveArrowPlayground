package de.amirrocker.responsivearrowplayground


import android.graphics.Color.GREEN
import arrow.core.Either
import arrow.core.getOrElse
import de.amirrocker.responsivearrowplayground.eventsourcing.Code
import de.amirrocker.responsivearrowplayground.eventsourcing.Code.Companion.codeWith
import de.amirrocker.responsivearrowplayground.eventsourcing.Code.Peg.Color.BLUE
import de.amirrocker.responsivearrowplayground.eventsourcing.Code.Peg.Color.PURPLE
import de.amirrocker.responsivearrowplayground.eventsourcing.Code.Peg.Color.RED
import de.amirrocker.responsivearrowplayground.eventsourcing.Code.Peg.Color.GREEN
import de.amirrocker.responsivearrowplayground.eventsourcing.Code.Peg.Color.PINK
import de.amirrocker.responsivearrowplayground.eventsourcing.Code.Peg.Color.YELLOW
import de.amirrocker.responsivearrowplayground.eventsourcing.Feedback
import de.amirrocker.responsivearrowplayground.eventsourcing.Feedback.Companion.feedbackWith
import de.amirrocker.responsivearrowplayground.eventsourcing.Feedback.Outcome.IN_PROGRESS
import de.amirrocker.responsivearrowplayground.eventsourcing.Feedback.Outcome.LOST
import de.amirrocker.responsivearrowplayground.eventsourcing.Feedback.Outcome.WON
import de.amirrocker.responsivearrowplayground.eventsourcing.Feedback.Peg.BLACK
import de.amirrocker.responsivearrowplayground.eventsourcing.Game
import de.amirrocker.responsivearrowplayground.eventsourcing.GameCommand
import de.amirrocker.responsivearrowplayground.eventsourcing.GameCommand.MakeGuess
import de.amirrocker.responsivearrowplayground.eventsourcing.GameError
import de.amirrocker.responsivearrowplayground.eventsourcing.GameError.GameFinishError.GameAlreadyLost
import de.amirrocker.responsivearrowplayground.eventsourcing.GameError.GuessError.GameNotStarted
import de.amirrocker.responsivearrowplayground.eventsourcing.GameError.GuessError.GuessTooLong
import de.amirrocker.responsivearrowplayground.eventsourcing.GameError.GuessError.GuessTooShort
import de.amirrocker.responsivearrowplayground.eventsourcing.GameError.GuessError.InvalidPegGuess
import de.amirrocker.responsivearrowplayground.eventsourcing.GameEvent
import de.amirrocker.responsivearrowplayground.eventsourcing.GameEvent.GameLost
import de.amirrocker.responsivearrowplayground.eventsourcing.GameEvent.GameStarted
import de.amirrocker.responsivearrowplayground.eventsourcing.GameEvent.GuessMade
import de.amirrocker.responsivearrowplayground.eventsourcing.Guess.Companion.guessWith
import de.amirrocker.responsivearrowplayground.eventsourcing.execute
import de.amirrocker.responsivearrowplayground.eventsourcing.notStartedGame
import de.amirrocker.responsivearrowplayground.eventsourcing.setOfPegs
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class EventSourcingPlaygroundTest {
    private val gameId = anyGameId()
    private val secret = Code(
        RED.value,
        Code.Peg.Color.GREEN.value,
        BLUE.value,
        YELLOW.value
    )
    private val totalAttempts = 12
    private val availablePegs = setOfPegs(
        RED.value,
        Code.Peg.Color.GREEN.value,
        BLUE.value,
        YELLOW.value,
        PURPLE.value,
        PINK.value,
    )

    private val game = gameOf(
        GameStarted(gameId, secret, totalAttempts, availablePegs)
    )

    @Test
    fun `it starts the game`() {

        execute(
            GameCommand.JoinGame(
                gameId = gameId,
                secret = secret,
                totalAttemps = totalAttempts,
                availablePegs = availablePegs
            )
        )
    }

    @Test
    fun `player makes a guess`() {
        execute(
            MakeGuess(
                gameId,
                Code(PURPLE.value, PURPLE.value, PURPLE.value, PURPLE.value)
            ), game
        ) shouldSucceedWith listOf(
            GuessMade(
                gameId,
                guessWith(
                    Code(PURPLE.value, PURPLE.value, PURPLE.value, PURPLE.value),
                    feedbackWith(IN_PROGRESS)
                )
            )
        )
    }

    @TestFactory
    fun `game gives feedback on the guess`() =
        guessExamples { (secret: Code, guess: Code, feedback: Feedback) ->
            val game = gameOf(GameStarted(gameId, secret, totalAttempts, availablePegs))
            execute(
                MakeGuess(gameId, guess), game
            ) shouldSucceedWith listOf(
                GuessMade(gameId, guessWith(guess, feedback))
            )
        }

    @Test
    fun `the game is won if the secret is guessed`() {
        execute(
            MakeGuess(gameId, secret),
            game
        ) shouldSucceedWith listOf(
            GuessMade(
                gameId,
                guessWith(
                    secret,
                    feedbackWith(WON, BLACK, BLACK, BLACK, BLACK)
                )
            ),
            GameEvent.GameWon(gameId)
        )
    }

    @Test
    fun `game is lost if the secret is not guessed within the number of attempts`() {
        val secret = codeWith(RED.value, Code.Peg.Color.GREEN.value, BLUE.value, YELLOW.value)
        val wrongCode = codeWith(PURPLE.value, PURPLE.value, PURPLE.value, PURPLE.value)
        val attempts = 3
        val game = gameOf(
            GameStarted(gameId, secret, attempts, availablePegs),
            GuessMade(gameId, guessWith(wrongCode, feedbackWith(IN_PROGRESS))),
            GuessMade(gameId, guessWith(wrongCode, feedbackWith(IN_PROGRESS)))
        )
        execute(MakeGuess(gameId, wrongCode), game) shouldSucceedWith listOf(
            GuessMade(gameId, guessWith(wrongCode, feedbackWith(LOST))),
            GameLost(gameId)
        )
    }

    @Test
    fun `game can no longer be played once it is won`() {
        val update = execute(MakeGuess(gameId, secret), game)
        val updatedGame = game.updated(update)

        execute(
            MakeGuess(gameId, secret),
            updatedGame
        ) shouldFailWith GameError.GameFinishError.GameAlreadyWon(gameId)
    }

    @Test
    fun `game can no longer be played once it is lost`() {
        val secret = codeWith(RED.value, Code.Peg.Color.GREEN.value, BLUE.value, YELLOW.value)
        val wrongCode = codeWith(PINK.value, PURPLE.value, PURPLE.value, PINK.value)
        val game = gameOf(GameStarted(gameId, secret, 1, availablePegs))
        val update = execute(MakeGuess(gameId, wrongCode), game)
        val updatedGame = game.updated(update)
        execute(MakeGuess(gameId, secret), updatedGame) shouldFailWith
                GameAlreadyLost(gameId)
    }

    @Test
    fun `game cannot be played if it is not yet started`() {
        val code = codeWith(RED.value, Code.Peg.Color.GREEN.value, BLUE.value, YELLOW.value)
        val game = notStartedGame()
        execute(MakeGuess(gameId, code), game) shouldFailWith GameNotStarted(gameId)
    }

    @Test
    fun `guess length cannot be shorter than secret`() {
        val secret = codeWith(RED.value, Code.Peg.Color.GREEN.value, BLUE.value, YELLOW.value)
        val code = codeWith(PINK.value, PINK.value, PINK.value)

        execute(MakeGuess(gameId, code), game) shouldFailWith GuessTooShort(gameId, code, secret.length)
    }

    @Test
    fun `guess length cannot be longer than secret`() {
        val secret = codeWith(RED.value, Code.Peg.Color.GREEN.value, BLUE.value, YELLOW.value)
        val code = codeWith(PINK.value, PINK.value, PINK.value, PURPLE.value, PURPLE.value)

        execute(MakeGuess(gameId, code), game) shouldFailWith GuessTooLong(gameId, code, secret.length)
    }

    @Test
    fun `game rejects pegs that game was not started with`() {
        val secret = codeWith(RED.value, Code.Peg.Color.GREEN.value, BLUE.value, BLUE.value)
        val availablePegs = setOfPegs(RED.value, Code.Peg.Color.GREEN.value, BLUE.value)
        val game = gameOf(GameStarted(gameId, secret, totalAttempts, availablePegs))
        val guess = codeWith(RED.value, Code.Peg.Color.GREEN.value, BLUE.value, YELLOW.value)

        execute(MakeGuess(gameId, guess), game) shouldFailWith InvalidPegGuess(gameId, guess, availablePegs)
    }
}

private fun gameOf(vararg events: GameEvent): Game = listOf(*events)

private fun Game.updated(update: Either<GameError, Game>): Game =
    this + update.getOrElse { emptyList() }

private fun guessExamples(block: (Triple<Code, Code, Feedback>) -> Unit) = mapOf(
    "game gives a black peg for each code peg on the correct position" to Triple(
        codeWith(RED.value, Code.Peg.Color.GREEN.value, BLUE.value, YELLOW.value),
        codeWith(RED.value, PURPLE.value, BLUE.value, BLUE.value),
        feedbackWith(IN_PROGRESS, BLACK, BLACK),
    ),
//    "game gives no black peg for code peg duplicated on a wrong position" to Triple(
//        codeWith(RED.value, GREEN.value, BLUE.value, YELLOW.value),
//        codeWith(RED.value, RED.value, PURPLE.value, PURPLE.value),
//        feedbackWith(IN_PROGRESS, BLACK),
//    ),
//    "game gives a white peg for code that is part of the secret code but is placed on a wrong position" to Triple(
//        codeWith(RED.value, GREEN.value, BLUE.value, YELLOW.value),
//        codeWith(PURPLE.value, RED.value, PURPLE.value, PURPLE.value),
//        feedbackWith(IN_PROGRESS, Feedback.Peg.WHITE),
//    ),
//    "game gives no white peg for code peg duplicated on a wrong position" to Triple(
//        codeWith(RED.value, GREEN.value, BLUE.value, YELLOW.value),
//        codeWith(PURPLE.value, RED.value, RED.value, PURPLE.value),
//        feedbackWith(IN_PROGRESS, Feedback.Peg.WHITE),
//    ),
//    "game gives a white peg for each code peg on a wrong position" to Triple(
//        codeWith(RED.value, GREEN.value, BLUE.value, YELLOW.value),
//        codeWith(RED.value, PURPLE.value, BLUE.value, BLUE.value),
//        feedbackWith(IN_PROGRESS, Feedback.Peg.WHITE, Feedback.Peg.WHITE),
//    ),

    ).dynamicTestsFor(block)
