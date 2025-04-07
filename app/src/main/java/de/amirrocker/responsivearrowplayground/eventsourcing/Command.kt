package de.amirrocker.responsivearrowplayground.eventsourcing

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
}