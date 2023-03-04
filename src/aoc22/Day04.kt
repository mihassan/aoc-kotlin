@file:Suppress("PackageDirectoryMismatch")

package aoc22.day04

import lib.Ranges.overlaps
import lib.Ranges.contains
import lib.Solution


typealias InputLine = Pair<IntRange, IntRange>

private val solution = object : Solution<List<InputLine>, Int>(2022, "Day04") {
  override fun parse(input: String): List<InputLine> = input.lines().map { line ->
    fun parseIntRange(str: String): IntRange {
      val (l, h) = str.split("-").map { it.toInt() }
      return l..h
    }

    val (part1, part2) = line.split(",")
    parseIntRange(part1) to parseIntRange(part2)
  }

  override fun format(output: Int): String = output.toString()

  override fun part1(input: List<Pair<IntRange, IntRange>>): Int = input.count { (p1, p2) ->
    (p1 in p2) || (p2 in p1)
  }

  override fun part2(input: List<Pair<IntRange, IntRange>>): Int = input.count { (p1, p2) ->
    p1 overlaps p2
  }
}

fun main() = solution.run()
