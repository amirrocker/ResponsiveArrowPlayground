package de.amirrocker.responsivearrowplayground.eventsourcing

/**
 * Credit and thank you for sharing this cool concept goes to this blogpost:
 * https://dev.to/jakub_zalas/functional-domain-model-o3j
 */

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import arrow.core.nonEmptyListOf
import arrow.core.right

typealias Game = List<GameEvent>

@JvmInline
value class GameId(val value: String)

fun execute(
    command: GameCommand,
    game: Game = notStartedGame()
): Either<GameError, NonEmptyList<GameEvent>> =
    when (command) {
        is GameCommand.JoinGame -> joinGame(command)
        is GameCommand.MakeGuess -> makeGuess(command, game).withOutcome()
    }

fun notStartedGame(): Game = emptyList()

private fun joinGame(command: GameCommand.JoinGame) =
    either<Nothing, NonEmptyList<GameEvent.GameStarted>> {
        nonEmptyListOf(
            GameEvent.GameStarted(
                command.gameId,
                command.secret,
                command.totalAttemps,
                command.availablePegs
            )
        )
    }

private fun makeGuess(command: GameCommand.MakeGuess, game: Game) =
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

private fun startedNotFinishedGame(
    command: GameCommand.MakeGuess,
    game: Game
): Either<GameError, Game> {
    if (!game.isStarted()) {
        return GameError.GuessError.GameNotStarted(command.gameId).left()
    }
    if (game.isWon()) {
        return GameError.GameFinishError.GameAlreadyWon(command.gameId).left()
    }
    if (game.isLost()) {
        return GameError.GameFinishError.GameAlreadyLost(command.gameId).left()
    }
    return game.right()
}

private fun validGuess(command: GameCommand.MakeGuess, game: Game): Either<GameError, Code> {
    if (game.isGuessTooShort(command.guess)) {
        return GameError.GuessError.GuessTooShort(command.gameId, command.guess, game.secretLength)
            .left()
    }
    if (game.isGuessTooLong(command.guess)) {
        return GameError.GuessError.GuessTooLong(command.gameId, command.guess, game.secretLength)
            .left()
    }
    if (!game.isGuessValid(command.guess)) {
        return GameError.GuessError.InvalidPegGuess(
            command.gameId,
            command.guess,
            game.availablePegs
        ).left()
    }
    return command.guess.right()
}

private fun Either<GameError, GameEvent.GuessMade>.withOutcome(): Either<GameError, NonEmptyList<GameEvent>> =
    map { event ->
        nonEmptyListOf<GameEvent>(event) +
                when (event.guess.feedback.outcome) {
                    Feedback.Outcome.WON -> listOf(GameEvent.GameWon(event.gameId))
                    Feedback.Outcome.LOST -> listOf(GameEvent.GameLost(event.gameId))
                    else -> emptyList()
                }
    }

private fun Game.feedbackOn(guess: Code): Feedback =
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
private fun Game.exactHits(guess: Code) = this.secretPegs
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


private fun <T> List<T>.remove(item: T): List<T>? = indexOf(item).let { index ->
    if (index != -1) filterIndexed { i, _ -> i != index }
    else null
}

fun setOfPegs(vararg pegs: String): Set<Code.Peg> = pegs.map(Code::Peg).toSet()