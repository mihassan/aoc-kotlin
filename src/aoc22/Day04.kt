package aoc22

import lib.Solution


typealias InputLine = Pair<IntRange, IntRange>

private operator fun IntRange.contains(other: IntRange): Boolean =
  other.first in this && other.last in this

private infix fun IntRange.overlaps(other: IntRange): Boolean {
  check(step > 0 && other.step > 0)
  return first <= other.last && last >= other.first
}

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

  override fun part1(input: List<InputLine>): Int = input.count { (p1, p2) ->
    (p1 in p2) || (p2 in p1)
  }

  override fun part2(input: List<InputLine>): Int = input.count { (p1, p2) ->
    p1 overlaps p2
  }
}

fun main() = solution.run()
