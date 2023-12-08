@file:Suppress("PackageDirectoryMismatch")

package aoc23.day03

import lib.Adjacency
import lib.Collections.groupContiguousBy
import lib.Grid
import lib.Path
import lib.Point
import lib.Solution

data class PartNumber(val value: Int, val path: Path)

data class Gear(val starLocation: Point, val partNumbers: List<PartNumber>) {
  fun value(): Int = partNumbers.fold(1) { acc, partNumber -> acc * partNumber.value }
}

data class EngineSchematic(val grid: Grid<Char>) {
  fun extractPartNumbers(): List<PartNumber> =
    findNumberLocations()
      .map(::toPartNumber)
      .filter(::isValidPartNumber)

  fun extractGears(): List<Gear> =
    extractPartNumbers()
      .flatMap(::findStarPartNumberPairs)
      .groupBy({ it.first }, { it.second })
      .map { Gear(it.key, it.value) }
      .filter(::isValidGear)

  private fun findNumberLocations(): List<List<Point>> = grid
    .indicesOf { it.isDigit() }
    .groupContiguousBy { pts, pt -> pts.last().right() == pt }

  private fun toPartNumber(points: List<Point>): PartNumber {
    val path = Path(points)
    val value = points.map { grid[it] }.joinToString("").toInt()
    return PartNumber(value, path)
  }

  private fun isValidPartNumber(partNumber: PartNumber): Boolean =
    grid
      .adjacents(partNumber.path, Adjacency.ALL)
      .any { grid[it].isSymbol() }

  private fun findStarPartNumberPairs(partNumber: PartNumber): List<Pair<Point, PartNumber>> =
    grid.adjacents(partNumber.path, Adjacency.ALL)
      .filter { grid[it].isStar() }
      .map { it to partNumber }

  private fun isValidGear(gear: Gear): Boolean = gear.partNumbers.size == 2

  private fun Char.isSymbol(): Boolean = !this.isDigit() && this != '.'

  private fun Char.isStar(): Boolean = this == '*'
}

typealias Input = EngineSchematic

typealias Output = Int

private val solution = object : Solution<Input, Output>(2023, "Day03") {
  override fun parse(input: String): Input =
    EngineSchematic(Grid(input.lines().map { it.toList() }))

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.extractPartNumbers().sumOf { it.value }

  override fun part2(input: Input): Output = input.extractGears().sumOf { it.value() }
}

fun main() = solution.run()
