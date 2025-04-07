package de.amirrocker.responsivearrowplayground.eventsourcing

data class Code(val pegs: List<Peg>) {
    constructor(vararg pegs: Peg) : this(pegs.toList())
    constructor(vararg pegs: String) : this(pegs.map(::Peg))

    data class Peg(val name: String) {

        enum class Color(val value: String) {
            RED("Red"),
            GREEN("Green"),
            BLUE("Blue"),
            YELLOW("Yellow"),
            PURPLE("Purple"),
            PINK("Pink"),

        }
    }

    val length: Int get() = pegs.size

    companion object {
        fun codeWith(
            pegs: List<Peg>
        ) = Code(
            pegs = pegs
        )
        fun codeWith(
            vararg pegs: Peg
        ) = Code(
            pegs = pegs
        )
        fun codeWith(
            vararg pegs: String
        ) = Code(
            pegs = pegs
        )
    }

}

data class Guess(val code: Code, val feedback: Feedback) {
    companion object {
        fun guessWith(
            code: Code, feedback: Feedback
        ) = Guess(
            code = code, feedback = feedback
        )
    }
}

data class Feedback(val outcome: Outcome, val pegs: List<Peg>) {
    constructor(outcome: Outcome, vararg pegs: Peg) : this(outcome, pegs.toList())

    enum class Peg {
        BLACK, WHITE;

        fun formattedName(): String = name.lowercase().replaceFirstChar(Char::uppercase)
    }

    enum class Outcome {
        IN_PROGRESS, WON, LOST
    }
    companion object {
        fun feedbackWith(
            outcome: Outcome, pegs: List<Peg>
        ) = Feedback(
            outcome = outcome, pegs = pegs
        )
        fun feedbackWith(
            outcome: Outcome, vararg pegs: Peg
        ) = Feedback(
            outcome = outcome, pegs = pegs
        )
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