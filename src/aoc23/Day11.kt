@file:Suppress("PackageDirectoryMismatch")

package aoc23.day11

import kotlin.math.abs
import lib.Grid
import lib.Point
import lib.Solution

typealias Input = Grid<Char>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2023, "Day11") {
  override fun parse(input: String): Input = Grid.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    val expandedImage: Input = input.expandRows().expandCols()
    val galaxies = expandedImage.indicesOf('#')
    return galaxies.sumOf { g1 ->
      galaxies.sumOf { g2 ->
        if (g1 < g2) distance(g1, g2) else 0
      }
    }
  }

  override fun part2(input: Input): Output {
    TODO("Not yet implemented")
  }

  private fun Input.expandRows(): Input =
    Grid(grid.flatMap { row ->
      if (row.all { it == '.' }) {
        listOf(row, row)
      } else {
        listOf(row)
      }
    })

  private fun Input.expandCols(): Input =
    transposed().expandRows().transposed()

  private fun distance(g1: Point, g2: Point): Int =
    (g1 - g2).let { (dx, dy) ->
      abs(dx) + abs(dy)
    }
}

fun main() = solution.run()
