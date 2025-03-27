@file:Suppress("PackageDirectoryMismatch")

package aoc21.day06

import lib.Collections.histogram
import lib.Solution

private typealias Input = List<Int>

private typealias Output = Long

private val solution = object : Solution<Input, Output>(2021, "Day06") {
  override fun parse(input: String): Input =
    input.split(",").map(String::toInt)

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output {
    val rounds = when (part) {
      Part.PART1 -> 80
      Part.PART2 -> 256
    }
    var ages = input.histogram().mapValues { it.value.toLong() }

    repeat(rounds) {
      val ageCounts = ages.entries.flatMap { (age, count) ->
        when (age) {
          0 -> listOf(6 to count, 8 to count)
          else -> listOf(age - 1 to count)
        }
      }
      ages = ageCounts.groupBy({ it.first }, { it.second }).mapValues { it.value.sum() }.toMap()
    }

    return ages.values.sum()
  }
}

fun main() = solution.run()
