@file:Suppress("PackageDirectoryMismatch")

package aoc23.day14

import lib.Solution

typealias Input = List<String>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2023, "Day14") {
  override fun parse(input: String): Input = input.lines()

  override fun format(output: Output): String = "$output"

  // Calculating load on right is equivalent to calculating load on up after rotating the grid 90
  // degrees clockwise. Also, the implementation is slightly simpler.
  override fun part1(input: Input): Output = input.rotateCW().tiltRight().loadOnRight()

  override fun part2(input: Input): Output {
    val cache = mutableMapOf<List<String>, Int>()
    var grid = input
    var count = 1
    while (true) {
      var nextGrid = grid.spinCycle()
      if (nextGrid in cache) {
        val cycleLength = count - cache[nextGrid]!!
        val remaining = (1000000000 - count) % cycleLength
        repeat(remaining) {
          nextGrid = nextGrid.spinCycle()
        }
        return nextGrid.rotateCW().loadOnRight()
      }
      cache[nextGrid] = count
      grid = nextGrid
      count++
    }
  }

  private fun List<String>.spinCycle(): List<String> =
    (1..4).fold(this) { grid, _ -> grid.rotateCW().tiltRight() }

  private fun List<String>.rotateCW(): List<String> =
    first().indices
      // Transpose the grid.
      .map { col -> map { it[col] }.joinToString("") }
      // Flip the grid horizontally.
      .map { it.reversed() }

  private fun List<String>.tiltRight(): List<String> = map { it.tiltRight() }

  private fun String.tiltRight(): String =
    split('#').joinToString("#") { it.tiltRightWithinBound() }

  private fun String.tiltRightWithinBound(): String = toCharArray().sorted().joinToString("")

  private fun List<String>.loadOnRight(): Int = sumOf { it.loadOnRight() }

  private fun String.loadOnRight(): Int = withIndex().sumOf { (i, c) -> if (c == 'O') i + 1 else 0 }
}

fun main() = solution.run()
