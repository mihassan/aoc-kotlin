@file:Suppress("PackageDirectoryMismatch")

package aoc23.day04

import kotlin.math.min
import lib.Maths.pow
import lib.Solution
import lib.Strings.extractInts
import lib.Strings.ints

data class Card(val id: Int, val winningNumbers: List<Int>, val numbersInHand: List<Int>) {
  val matches: Int by lazy {
    numbersInHand.count { it in winningNumbers }
  }

  val point: Int by lazy {
    (2 pow matches) / 2
  }

  companion object {
    fun parse(cardStr: String): Card {
      val (idPart, winPart, handPart) = cardStr.split(":", "|")

      val id = idPart.extractInts().single()
      val winningNumbers = winPart.ints()
      val numbersInHand = handPart.ints()

      return Card(id, winningNumbers, numbersInHand)
    }
  }
}

typealias Input = List<Card>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2023, "Day04") {
  override fun parse(input: String): Input = input.lines().map { Card.parse(it) }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.sumOf(Card::point)

  override fun part2(input: Input): Output {
    val n = input.size
    val cards = (1..n).associateWith { 1 }.toMutableMap()

    for (id in 1..n) {
      val m = input[id - 1].matches
      for (id2 in id + 1..min(id + m, n)) {
        cards[id2] = cards[id]!! + cards[id2]!!
      }
    }

    return cards.values.sum()
  }
}

fun main() = solution.run()
