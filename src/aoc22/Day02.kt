@file:Suppress("PackageDirectoryMismatch")

package aoc22.day02

import aoc22.GameResult.DRAW
import aoc22.GameResult.LOSS
import aoc22.GameResult.WIN
import aoc22.Hand.PAPER
import aoc22.Hand.ROCK
import aoc22.Hand.SCISSOR
import lib.Solution
import lib.Solution.Part
import lib.Solution.Part.PART1
import lib.Solution.Part.PART2


enum class Hand {
  ROCK, PAPER, SCISSOR;

  fun score(): Int = when (this) {
    ROCK -> 1
    PAPER -> 2
    SCISSOR -> 3
  }

  companion object {
    fun of(hand: String): Hand = when (hand) {
      "A" -> ROCK
      "B" -> PAPER
      "C" -> SCISSOR
      else -> throw IllegalArgumentException("Bad input for a hand: $hand")
    }
  }
}

enum class Strategy {
  X, Y, Z;

  fun handToPlay(part: Part, opponent: Hand): Hand = when (part) {
    PART1 -> handToPlayForPart1()
    PART2 -> handToPlayForPart2(opponent)
  }

  private fun handToPlayForPart1(): Hand = when (this) {
    X -> ROCK
    Y -> PAPER
    Z -> SCISSOR
  }

  private fun handToPlayForPart2(opponent: Hand): Hand = when (this) {
    X -> when (opponent) {
      ROCK -> SCISSOR
      PAPER -> ROCK
      SCISSOR -> PAPER
    }
    Y -> opponent
    Z -> when (opponent) {
      ROCK -> PAPER
      PAPER -> SCISSOR
      SCISSOR -> ROCK
    }
  }

  companion object {
    fun of(strategy: String): Strategy = when (strategy) {
      "X" -> X
      "Y" -> Y
      "Z" -> Z
      else -> throw IllegalArgumentException("Bad input for a strategy: $strategy")
    }
  }
}

enum class GameResult {
  WIN, LOSS, DRAW;

  fun score(): Int = when (this) {
    WIN -> 6
    LOSS -> 0
    DRAW -> 3
  }
}

data class Game(val opponent: Hand, val strategy: Strategy) {

  fun result(part: Part): GameResult {
    val myHand = strategy.handToPlay(part, opponent)
    return when (myHand to opponent) {
      ROCK to ROCK -> DRAW
      ROCK to PAPER -> LOSS
      ROCK to SCISSOR -> WIN
      PAPER to ROCK -> WIN
      PAPER to PAPER -> DRAW
      PAPER to SCISSOR -> LOSS
      SCISSOR to ROCK -> LOSS
      SCISSOR to PAPER -> WIN
      SCISSOR to SCISSOR -> DRAW
      else -> throw java.lang.AssertionError()
    }
  }

  companion object {
    fun of(game: String): Game {
      val (hand, strategy) = game.split(" ")
      return Game(Hand.of(hand), Strategy.of(strategy))
    }
  }
}


private val solution = object : Solution<List<Game>, Int>(2022, "Day02") {
  override fun parse(input: String): List<Game> =
    input.split("\n").filter { it.isNotBlank() }.map { Game.of(it) }

  override fun format(output: Int): String = output.toString()

  override fun solve(part: Part, input: List<Game>): Int = input.sumOf { game: Game ->
    val myHand = game.strategy.handToPlay(part, game.opponent)
    myHand.score() + game.result(part).score()
  }
}

fun main() = solution.run()
