package de.amirrocker.responsivearrowplayground.eventsourcing

internal val Game.secret: Code?
    get() = filterIsInstance<GameEvent.GameStarted>().firstOrNull()?.secret

internal val Game.secretLength: Int
    get() = secret?.length ?: 0

internal val Game.secretPegs: List<Code.Peg>
    get() = secret?.pegs ?: emptyList()

/**
 * How many guesses are already in the event queue
 */
internal val Game.attempts: Int
    get() = filterIsInstance<GameEvent.GuessMade>().size

internal val Game.totalAttempts: Int
    get() = filterIsInstance<GameEvent.GameStarted>().firstOrNull()?.totalAttemps ?: 0

internal val Game.availablePegs: Set<Code.Peg>
    get() = filterIsInstance<GameEvent.GameStarted>().firstOrNull()?.availablePegs ?: emptySet()

internal fun Game.isWon(): Boolean =
    filterIsInstance<GameEvent.GameWon>().isNotEmpty()

internal fun Game.isLost(): Boolean =
    filterIsInstance<GameEvent.GameLost>().isNotEmpty()

internal fun Game.isStarted(): Boolean =
    filterIsInstance<GameEvent.GameStarted>().isNotEmpty()

internal fun Game.isGuessTooLong(guess: Code): Boolean =
    guess.length > this.secretLength

internal fun Game.isGuessTooShort(guess: Code): Boolean =
    guess.length < this.secretLength

internal fun Game.isGuessValid(guess: Code): Boolean =
    availablePegs.containsAll(guess.pegs)