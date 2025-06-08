@file:Suppress("PackageDirectoryMismatch")

package aoc21.day21

import kotlin.math.max
import lib.Collections.histogram
import lib.Combinatorics.permutationsWithReplacement
import lib.Solution

private enum class Player { PLAYER1, PLAYER2 }

private data class PlayerState(val position: Int, val score: Int) {
  fun move(steps: Int): PlayerState {
    val newPosition = (position + steps - 1) % MAX_POSITIONS + 1
    return PlayerState(newPosition, score + newPosition)
  }

  companion object {
    private const val MAX_POSITIONS = 10
  }
}

private data class GameState(
  val player1: PlayerState,
  val player2: PlayerState,
  val currentPlayer: Player,
) {
  fun move(steps: Int): GameState =
    when (currentPlayer) {
      Player.PLAYER1 -> copy(player1 = player1.move(steps), currentPlayer = Player.PLAYER2)
      Player.PLAYER2 -> copy(player2 = player2.move(steps), currentPlayer = Player.PLAYER1)
    }

  fun winner(winningScore: Int): Player? =
    when {
      player1.score >= winningScore -> Player.PLAYER1
      player2.score >= winningScore -> Player.PLAYER2
      else -> null
    }

  fun isRunning(winningScore: Int): Boolean = winner(winningScore) == null

  fun loserScore(winningScore: Int): Int? =
    when (winner(winningScore)) {
      Player.PLAYER1 -> player2.score
      Player.PLAYER2 -> player1.score
      null -> null
    }

  companion object {
    fun initial(p1Start: Int, p2Start: Int): GameState =
      GameState(PlayerState(p1Start, 0), PlayerState(p2Start, 0), Player.PLAYER1)

    fun parse(input: String): GameState {
      val lines = input.lines()
      val p1Start = lines[0].substringAfterLast(" ").toInt()
      val p2Start = lines[1].substringAfterLast(" ").toInt()
      return initial(p1Start, p2Start)
    }
  }
}

private data class DeterministicDice(private var current: Int = 1) {
  fun roll(): Int {
    val result = current
    current = if (current == SIDES) 1 else current + 1
    return result
  }

  companion object {
    private const val SIDES = 100
  }
}

private data class GameResult(val player1Wins: Long, val player2Wins: Long) {
  operator fun plus(other: GameResult): GameResult =
    GameResult(player1Wins + other.player1Wins, player2Wins + other.player2Wins)

  operator fun times(count: Int): GameResult =
    GameResult(player1Wins * count, player2Wins * count)

  companion object {
    val ZERO = GameResult(0, 0)
  }
}

private typealias Input = GameState
private typealias Output = Long

private val solution = object : Solution<Input, Output>(2021, "Day21") {
  override fun parse(input: String): Input = GameState.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    var game = input
    val dice = DeterministicDice()
    var rolls = 0L

    val winningScore = 1000

    while (game.isRunning(winningScore)) {
      val steps = dice.roll() + dice.roll() + dice.roll()
      game = game.move(steps)
      rolls += 3
    }

    val losingPlayerScore = game.loserScore(winningScore) ?: error("Game should be finished here")

    return rolls * losingPlayerScore
  }

  override fun part2(input: Input): Output {
    val winCountCache = mutableMapOf<GameState, GameResult>()
    val stepCounts = permutationsWithReplacement(setOf(1, 2, 3), 3).map { it.sum() }.histogram()

    fun winCount(game: GameState): GameResult {
      if (game in winCountCache) {
        return winCountCache[game]!!
      }

      if (game.winner(21) != null) {
        return when (game.winner(21)) {
          Player.PLAYER1 -> GameResult(1, 0)
          Player.PLAYER2 -> GameResult(0, 1)
          null -> GameResult.ZERO
        }
      }

      val results = stepCounts.map { (steps, count) -> winCount(game.move(steps)) * count }
      val result = results.reduce(GameResult::plus)

      winCountCache[game] = result
      return result
    }

    val result = winCount(input)
    return max(result.player1Wins, result.player2Wins)
  }
}

fun main() = solution.run()
