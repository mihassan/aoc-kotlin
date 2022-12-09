@file:Suppress("PackageDirectoryMismatch")

package aoc22.day03

import lib.Solution
import lib.Strings.intersect
import lib.Strings.splitIn


private val solution = object : Solution<List<String>, Int>(2022, "Day03") {
  override fun parse(input: String): List<String> = input.lines()

  override fun format(output: Int): String = output.toString()

  override fun part1(input: List<String>): Int =
    input.sumOf {
      val (x, y) = it.splitIn(2)
      (x intersect y).single().priority
    }

  override fun part2(input: List<String>): Int =
    input.chunked(3).sumOf { (x, y, z) ->
      (x intersect y intersect z).single().priority
    }

  private val Char.priority
    get() = when (this) {
      in 'a'..'z' -> this - 'a' + 1
      in 'A'..'Z' -> this - 'A' + 27
      else -> error("Check input")
    }
}

fun main() = solution.run()
