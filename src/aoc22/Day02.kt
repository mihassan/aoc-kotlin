@file:Suppress("PackageDirectoryMismatch")

package aoc22.day02

import aoc22.day02.GameResult.DRAW
import aoc22.day02.GameResult.LOSS
import aoc22.day02.GameResult.WIN
import aoc22.day02.Hand.PAPER
import aoc22.day02.Hand.ROCK
import aoc22.day02.Hand.SCISSOR
import lib.Solution
import lib.Solution.Part
import lib.Solution.Part.PART1
import lib.Solution.Part.PART2


enum class Hand(val score: Int) {
  ROCK(1), PAPER(2), SCISSOR(3);

  fun beats(): Hand = when (this) {
    ROCK -> SCISSOR
    PAPER -> ROCK
    SCISSOR -> PAPER
  }

  fun beatenBy(): Hand = when (this) {
    ROCK -> PAPER
    PAPER -> SCISSOR
    SCISSOR -> ROCK
  }

  companion object {
    fun of(hand: String): Hand = when (hand) {
      "A" -> ROCK
      "B" -> PAPER
      "C" -> SCISSOR
      else -> error("Bad input for a hand: $hand")
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
    X -> opponent.beats() // Loss
    Y -> opponent // Draw
    Z -> opponent.beatenBy() // Win
  }

  companion object {
    fun of(strategy: String): Strategy = when (strategy) {
      "X" -> X
      "Y" -> Y
      "Z" -> Z
      else -> error("Bad input for a strategy: $strategy")
    }
  }
}

enum class GameResult(val score: Int) {
  WIN(6), LOSS(0), DRAW(3)
}

fun Hand.play(other: Hand): GameResult = when (other) {
  beats() -> WIN
  beatenBy() -> LOSS
  else -> DRAW
}

data class Game(val opponent: Hand, val strategy: Strategy) {
  fun totalScore(part: Part): Int {
    val myHand = strategy.handToPlay(part, opponent)
    return myHand.score + myHand.play(opponent).score
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
    input.lines().filter { it.isNotBlank() }.map { Game.of(it) }

  override fun format(output: Int): String = output.toString()

  override fun solve(part: Part, input: List<Game>): Int = input.sumOf { game ->
    game.totalScore(part)
  }
}

fun main() = solution.run()
