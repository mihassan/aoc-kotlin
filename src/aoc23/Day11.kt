@file:Suppress("PackageDirectoryMismatch")

package aoc23.day11

import kotlin.math.abs
import lib.Collections.cumulativeSum1
import lib.Grid
import lib.Point
import lib.Solution

data class Image(val galaxies: List<Point>, val maxBound: Point) {
  fun expandRows(count: Int): Image {
    val rowSizes = (0..maxBound.y).map { if (isEmptyRow(it)) count else 1 }
    val lastRow = rowSizes.last()
    val newRows = rowSizes.cumulativeSum1().dropLast(1)
    val newGalaxies = galaxies.map { it.copy(y = newRows[it.y]) }
    return Image(newGalaxies, maxBound.copy(y = lastRow))
  }

  fun expandCols(count: Int): Image = transpose().expandRows(count).transpose()

  private fun isEmptyRow(row: Int) = galaxies.none { galaxy -> galaxy.y == row }

  private fun transpose() = Image(galaxies.map { it.transpose() }, maxBound.transpose())

  private fun Point.transpose() = Point(y, x)

  companion object {
    fun parse(input: String): Image {
      val grid = Grid.parse(input)
      val galaxies = grid.indicesOf('#')
      val maxBound = Point(galaxies.maxOf { it.x }, galaxies.maxOf { it.y })
      return Image(galaxies, maxBound)
    }
  }
}

typealias Input = Image

typealias Output = Long

private val solution = object : Solution<Input, Output>(2023, "Day11") {
  override fun parse(input: String): Input = Image.parse(input)

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output =
    with(expand(part, input)) {
      galaxies.sumOf { g1 ->
        galaxies.sumOf { g2 ->
          if (g1 < g2) g1.manhattanDistance(g2).toLong() else 0L
        }
      }
    }

  private fun expand(part: Part, input: Input): Input =
    EXPANSION_FACTOR[part]!!.let { factor ->
      input.expandRows(factor).expandCols(factor)
    }

  private fun distance(p1: Point, p2: Point): Long =
    (abs(p1.x - p2.x) + abs(p1.y - p2.y)).toLong()

  private val EXPANSION_FACTOR = mapOf(
    Part.PART1 to 2,
    Part.PART2 to 1000000
  )
}

fun main() = solution.run()
