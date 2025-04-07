package de.amirrocker.responsivearrowplayground

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import arrow.core.nonEmptyListOf
import arrow.core.right

@JvmInline
value class GameId(val value: String)

//sealed interface Command {


sealed interface GameCommand {

    val gameId: GameId

    data class JoinGame(
        override val gameId: GameId,
        val secret: Code,
        val totalAttemps: Int,
        val availablePegs: Set<Code.Peg>
    ) : GameCommand

    data class MakeGuess(
        override val gameId: GameId,
        val guess: Code
    ) : GameCommand

//        @JvmInline
//        value class NewGame(
//            override val gameId: GameId
//        ) : GameCommand
//
//        @JvmInline
//        value class LoadGame(
//            override val gameId: GameId
//        ) : GameCommand
//
//        data class SaveGame(
//            override val gameId: GameId,
////            val gameState: GameState
//        ) : GameCommand
//
//        data class WinGame(
//            override val gameId: GameId,
////            val gameState: GameState
//        ) : GameCommand
}

//    companion object {
//        fun newGameCommand() = GameCommand.NewGame(GameId("1"))
//        fun winGameCommand() = GameCommand.WinGame(GameId("1"))
//        fun loadGameCommand() = GameCommand.LoadGame(GameId("1"))
//        fun saveGameCommand() = GameCommand.SaveGame(GameId("1"))
//    }

//}

sealed interface GameEvent {

    val gameId: GameId

    data class GameStarted(
        override val gameId: GameId,
        val secret: Code,
        val totalAttemps: Int,
        val availablePegs: Set<Code.Peg>
    ) : GameEvent

    data class GuessMade(
        override val gameId: GameId,
        val guess: Guess
    ) : GameEvent

    @JvmInline
    value class GameWon(
        override val gameId: GameId
    ) : GameEvent

    @JvmInline
    value class GameLost(
        override val gameId: GameId
    ) : GameEvent
}

data class Code(val pegs: List<Peg>) {
    constructor(vararg pegs: Peg) : this(pegs.toList())
    constructor(vararg pegs: String) : this(pegs.map(::Peg))

    data class Peg(val name: String)

    val length: Int get() = pegs.size
}

data class Guess(val code: Code, val feedback: Feedback)

data class Feedback(val outcome: Outcome, val pegs: List<Peg>) {
    constructor(outcome: Outcome, vararg pegs: Peg) : this(outcome, pegs.toList())

    enum class Peg {
        BLACK, WHITE;

        fun formattedName(): String = name.lowercase().replaceFirstChar(Char::uppercase)
    }

    enum class Outcome {
        IN_PROGRESS, WON, LOST
    }
}

sealed interface GameError {
    val gameId: GameId

    sealed interface GameFinishError : GameError {
        data class GameAlreadyWon(override val gameId: GameId) : GameFinishError
        data class GameAlreadyLost(override val gameId: GameId) : GameFinishError
    }

    sealed interface GuessError : GameError {
        data class GameNotStarted(override val gameId: GameId) : GuessError
        data class GuessTooShort(
            override val gameId: GameId,
            val guess: Code,
            val requiredLength: Int
        ) : GuessError

        data class GuessTooLong(
            override val gameId: GameId,
            val guess: Code,
            val requiredLength: Int
        ) : GuessError

        data class InvalidPegGuess(
            override val gameId: GameId,
            val guess: Code,
            val availablePegs: Set<Code.Peg>
        ) : GuessError
    }
}

typealias Game = List<GameEvent>

private val Game.secret: Code?
    get() = filterIsInstance<GameEvent.GameStarted>().firstOrNull()?.secret

private val Game.secretLength: Int
    get() = secret?.length ?: 0

private val Game.secretPegs: List<Code.Peg>
    get() = secret?.pegs ?: emptyList()

/**
 * How many guesses are already in the event queue
 */
private val Game.attempts: Int
    get() = filterIsInstance<GameEvent.GuessMade>().size

private val Game.totalAttempts: Int
    get() = filterIsInstance<GameEvent.GameStarted>().firstOrNull()?.totalAttemps ?: 0

private val Game.availablePegs: Set<Code.Peg>
    get() = filterIsInstance<GameEvent.GameStarted>().firstOrNull()?.availablePegs ?: emptySet()

private fun Game.isWon(): Boolean =
    filterIsInstance<GameEvent.GameWon>().isNotEmpty()

private fun Game.isLost(): Boolean =
    filterIsInstance<GameEvent.GameLost>().isNotEmpty()

private fun Game.isStarted(): Boolean =
    filterIsInstance<GameEvent.GameStarted>().isNotEmpty()

private fun Game.isGuessTooLong(guess: Code): Boolean =
    guess.length > this.secretLength

private fun Game.isGuessTooShort(guess: Code): Boolean =
    guess.length < this.secretLength

private fun Game.isGuessValid(guess:Code):Boolean =
    availablePegs.containsAll(guess.pegs)

// execute event sourcing
fun execute(
    command: GameCommand,
    game: Game = notStartedGame()
): Either<GameError, NonEmptyList<GameEvent>> =
    when(command) {
        is GameCommand.JoinGame -> joinGame(command)
        is GameCommand.MakeGuess -> makeGuess(command, game).withOutcome()
    }

fun notStartedGame(): Game = emptyList()


private fun joinGame(command: GameCommand.JoinGame) = either<Nothing, NonEmptyList<GameEvent.GameStarted>> {
    nonEmptyListOf(
        GameEvent.GameStarted(
            command.gameId,
            command.secret,
            command.totalAttemps,
            command.availablePegs
        )
    )
}

private fun makeGuess(command:GameCommand.MakeGuess, game:Game) =
    startedNotFinishedGame(command, game)
        .flatMap { startedGame ->
            validGuess(command, game)
                .map { guess ->
                    GameEvent.GuessMade(
                        command.gameId,
                        Guess(
                            command.guess,
                            startedGame.feedbackOn(guess)
                        )
                    )
        }
    }

private fun startedNotFinishedGame(command: GameCommand.MakeGuess, game: Game) : Either<GameError, Game> {
    if(!game.isStarted()) {
        return GameError.GuessError.GameNotStarted(command.gameId).left()
    }
    if(game.isWon()) {
        return GameError.GameFinishError.GameAlreadyWon(command.gameId).left()
    }
    if(game.isLost()) {
        return GameError.GameFinishError.GameAlreadyLost(command.gameId).left()
    }
    return game.right()
}

private fun validGuess(command: GameCommand.MakeGuess, game: Game): Either<GameError, Code> {
    if(game.isGuessTooShort(command.guess)) {
        return GameError.GuessError.GuessTooShort(command.gameId, command.guess, game.secretLength).left()
    }
    if(game.isGuessTooLong(command.guess)) {
        return GameError.GuessError.GuessTooLong(command.gameId, command.guess, game.secretLength).left()
    }
    if(!game.isGuessValid(command.guess)) {
        return GameError.GuessError.InvalidPegGuess(command.gameId, command.guess, game.availablePegs).left()
    }
    return command.guess.right()
}

private fun Either<GameError, GameEvent.GuessMade>.withOutcome(): Either<GameError, NonEmptyList<GameEvent>> =
    map { event ->
        nonEmptyListOf<GameEvent>(event) +
                when(event.guess.feedback.outcome) {
                    Feedback.Outcome.WON -> listOf(GameEvent.GameWon(event.gameId))
                    Feedback.Outcome.LOST -> listOf(GameEvent.GameLost(event.gameId))
                    else -> emptyList()
                }
    }

private fun Game.feedbackOn(guess:Code): Feedback =
    feedbackPegsOn(guess)
        .let { (exactHits, colorHits) ->
            Feedback(outcomeFor(exactHits), exactHits + colorHits)
        }

private fun Game.outcomeFor(exactHits: List<Feedback.Peg>) = when {
    exactHits.size == this.secretLength -> Feedback.Outcome.WON
    this.attempts + 1 == this.totalAttempts -> Feedback.Outcome.LOST
    else -> Feedback.Outcome.IN_PROGRESS
}

private fun Game.feedbackPegsOn(guess: Code) =
    exactHits(guess).map {
        Feedback.Peg.BLACK
    } to colorHits(guess).map {
        Feedback.Peg.WHITE
    }

//
private fun Game.exactHits(guess:Code) = this.secretPegs
    .zip(guess.pegs)
    .filter { (secretColor, guessColor) -> secretColor == guessColor }
    .unzip()
    .second

private fun Game.colorHits(guess: Code) = this.secretPegs
    .zip(guess.pegs)
    .filter { (secretColor, guessColor) -> secretColor != guessColor }
    .unzip()
    .let { (secret, guess) ->
        guess.fold(secret to emptyList<Code.Peg>()) { (secretPegs, colorHits), guessPeg ->
            secretPegs.remove(guessPeg)?.let {
                it to colorHits + guessPeg
            } ?: (secretPegs to colorHits)
        }.second
    }



private fun <T> List<T>.remove(item: T) : List<T>? = indexOf(item).let { index ->
    if(index != -1) filterIndexed { i, _ -> i != index }
    else null
}

//typealias Error = String
//typealias Game = NonEmptyList<GameEvent>
//
//fun execute(command: Command, game: Game): Either<Error, NonEmptyList<GameEvent>> =
//    when (command) {
//        is Command.GameCommand.NewGame ->
//            nonEmptyListOf(GameStarted(gameId = GameId("1"))).right() // println("start new Game event called")
//        is Command.GameCommand.LoadGame ->
//            nonEmptyListOf(GameLoaded(gameId = GameId("1"))).right() // println("load Game event called")
//        is Command.GameCommand.SaveGame ->
//            nonEmptyListOf(GameSaved(gameId = GameId("1"))).right() // println("save Game event called")
//        is Command.GameCommand.WinGame ->
//            nonEmptyListOf(GameEvent.GameWon(gameId = GameId("1"))).right() // println("save Game event called")
//    }
//
//fun Game.isGameWon() = this.filterIsInstance<GameEvent.GameWon>().isNotEmpty()
//
//fun startGame() = nonEmptyListOf<GameEvent>(
//    head = GameStarted(GameId("1"))
//)

fun tst() {

}