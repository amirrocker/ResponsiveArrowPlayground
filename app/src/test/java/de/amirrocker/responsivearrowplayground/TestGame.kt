package de.amirrocker.responsivearrowplayground

import arrow.core.nonEmptyListOf
import de.amirrocker.responsivearrowplayground.Command.GameCommand
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TestGame {


    // TODO switch to Junit5 with kluent
//    @Test
//    fun `given a GameStarted event then a GameStarted Event is expected`() {
//
//        // assert execute(Command, Error, Event) -> Either<Error, NonEmptyList<Event>>
//        val game = nonEmptyListOf<GameEvent>(GameEvent.GameStarted(GameId("")))
//        val command = GameCommand.NewGame(GameId("1234"))
//        val events = execute(command, game)
//
//        assertEquals(nonEmptyListOf(GameEvent.GameStarted(GameId("1"))), events.getOrNull())
//    }
//
//    @Test
//    fun `given a GameWon event then the game is won`() {
//        val game = startGame() /*nonEmptyListOf<GameEvent>(
//            head = GameEvent.GameStarted(GameId("1"))
//        )*/
//        val command = Command.winGameCommand() // GameCommand.WinGame(GameId("1"))
//
//        val result = execute(command, game)
//
//        val isWon = result.getOrNull()?.isGameWon()
//
//        assertEquals("", true, isWon)
//    }
//
//    @Test
//    fun `given a number of events apply events to update state`() {
//        val game = startGame() // nonEmptyListOf(GameEvent.GameStarted(GameId("1")))
//        val command = Command.loadGameCommand()
//    }



}