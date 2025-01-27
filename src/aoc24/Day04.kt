@file:Suppress("PackageDirectoryMismatch")

package aoc24.day04

import lib.Adjacency
import lib.Grid
import lib.Point
import lib.Solution

typealias Input = Grid<Char>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2024, "Day04") {
  override fun parse(input: String): Input = Grid.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.indices().sumOf { start ->
      Point(0, 0).adjacents(Adjacency.ALL).mapNotNull { offset ->
        input.getWord(start, offset, 4)
      }.count { it == "XMAS" }
    }

  override fun part2(input: Input): Output {
    val offsets = listOf(
      Point(1, 1) to Point(1, -1),
      Point(1, 1) to Point(-1, 1),
      Point(-1, -1) to Point(1, -1),
      Point(-1, -1) to Point(-1, 1)
    )

    return input.indices().sumOf { start ->
      offsets.mapNotNull { (o1, o2) ->
        input.getCrossWord(start, o1, o2, 3)
      }.count { (w1, w2) -> w1 == "MAS" && w2 == "MAS" }
    }
  }

  private fun Grid<Char>.getWord(start: Point, offset: Point, len: Int): String? =
    (0..<len)
      .mapNotNull { this[start + offset * it] }
      .toCharArray()
      .concatToString()
      .takeIf { it.length == len }

  private fun Grid<Char>.getCrossWord(
    start: Point,
    offset1: Point,
    offset2: Point,
    len : Int,
  ): Pair<String, String>? {
    val w1 = getWord(start - offset1 * (len / 2), offset1, len)
    val w2 = getWord(start - offset2 * (len / 2), offset2, len)

    if (w1 == null || w2 == null)
      return null

    return w1 to w2
  }
}

fun main() = solution.run()
