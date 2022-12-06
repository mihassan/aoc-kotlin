@file:Suppress("PackageDirectoryMismatch")

package aoc22.day06

import lib.Collections.isDistinct
import lib.Solution
import lib.Solution.Part.PART1
import lib.Solution.Part.PART2

private val solution = object : Solution<String, Int>(2022, "Day06") {
  override fun parse(input: String): String = input

  override fun format(output: Int): String = "$output"

  override fun solve(part: Part, input: String): Int {
    val message = input.toCharArray().toList()
    val markerSize = markerSize(part)
    val markerPosition = message.windowed(markerSize).indexOfFirst { it.isDistinct() }
    return markerPosition + markerSize
  }

  private fun markerSize(part: Part) = when (part) {
    PART1 -> 4
    PART2 -> 14
  }
}

fun main() = solution.run()
