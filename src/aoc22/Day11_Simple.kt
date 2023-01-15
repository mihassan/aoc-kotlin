@file:Suppress("PackageDirectoryMismatch")

package aoc22.day11_simple

import lib.Maths.divisibleBy
import lib.Solution
import lib.Strings.extractInts
import lib.Strings.extractLongs
import lib.Strings.words

data class Monkey(
  val startingItems: List<Long>,
  val operation: (Long) -> Long,
  val divisor: Long,
  val test: (Long) -> Int,
) {
  companion object {
    fun parse(str: String): Monkey {
      val lines = str.lines()

      val startingItems = lines[1].extractLongs()

      val (_, op, operand) = lines[2].substringAfter("new = ").words()
      val operation: (Long) -> Long = when (op) {
        "*" -> { old -> old * (operand.toLongOrNull() ?: old) }
        "+" -> { old -> old + (operand.toLongOrNull() ?: old) }
        else -> error("Invalid input")
      }

      val (divisor, trueTarget, falseTarget) = lines.takeLast(3).joinToString().extractInts()
      val test: (Long) -> Int = { worryLevel ->
        if (worryLevel divisibleBy divisor.toLong()) trueTarget else falseTarget
      }

      return Monkey(startingItems, operation, divisor.toLong(), test)
    }
  }
}

typealias Input = List<Monkey>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2022, "Day11") {
  val ROUNDS = mapOf(Part.PART1 to 20, Part.PART2 to 10000)

  override fun parse(input: String): Input = input.split("\n\n").map { Monkey.parse(it) }

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output {
    val modulus = input.map { it.divisor }.toSet().reduce(Long::times)
    val inspections = LongArray(input.size)
    val monkeysWithCurrentItems = input.map { it.startingItems.toMutableList() }

    repeat(ROUNDS[part]!!) {
      monkeysWithCurrentItems.forEachIndexed { idx, items ->
        val monkey = input[idx]

        items.forEach { item ->
          val worryLevel = when(part) {
            Part.PART1 -> monkey.operation(item) / 3L
            Part.PART2 -> monkey.operation(item) % modulus
          }

          val nextMonkey = monkey.test(worryLevel)

          monkeysWithCurrentItems[nextMonkey] += worryLevel
          inspections[idx]++
        }

        items.clear()
      }
    }

    return inspections.sortedDescending().take(2).reduce(Long::times)
  }
}

fun main() = solution.run()
