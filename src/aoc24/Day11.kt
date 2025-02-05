@file:Suppress("PackageDirectoryMismatch")

package aoc24.day11

import lib.Collections.histogram
import lib.Maths.isEven
import lib.Solution
import lib.Strings.longs
import lib.Strings.splitIn

typealias Input = List<Long>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2024, "Day11") {
  override fun parse(input: String): Input = input.longs()

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output {
    val initialStones = input.histogram().mapValues { it.value.toLong() }
    val repeatCount = when (part) {
      Part.PART1 -> 25
      Part.PART2 -> 75
    }
    return generateSequence(initialStones, ::processStones).elementAt(repeatCount).values.sum()
  }

  private fun processStones(stones: Map<Long, Long>): Map<Long, Long> =
    stones.flatMap { (stone, count) ->
        processStone(stone).map { it to count }
      }.groupBy({ it.first }, { it.second }).mapValues { (_, counts) -> counts.sum() }

  private fun processStone(stone: Long): List<Long> = when {
    stone == 0L -> listOf(1L)
    "$stone".length.isEven() -> "$stone".splitIn(2).map(String::toLong)
    else -> listOf(stone * 2024)
  }
}

fun main() = solution.run()
