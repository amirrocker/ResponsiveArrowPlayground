package de.amirrocker.responsivearrowplayground.eventsourcing

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