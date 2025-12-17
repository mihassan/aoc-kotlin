@file:Suppress("PackageDirectoryMismatch")

package aoc25.day04

import lib.Adjacency
import lib.Solution
import lib.Grid
import lib.Point

enum class Cell {
  EMPTY, PAPER;

  companion object {
    fun fromChar(c: Char): Cell = when (c) {
      '.' -> EMPTY
      '@' -> PAPER
      else -> error("Invalid cell character: $c")
    }
  }
}

typealias Input = Grid<Cell>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2025, "Day04") {
  private val MIN_ADJACENT_TO_STAY = 4

  override fun parse(input: String): Input = Grid.parse(input).map(Cell::fromChar)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = findPapersToRemove(input).size

  override fun part2(input: Input): Output {
    var grid = input

    while (true) {
      val papersToRemove = findPapersToRemove(grid)
      if (papersToRemove.isEmpty()) break
      grid = removePapers(grid, papersToRemove)
    }

    return input.count { it == Cell.PAPER } - grid.count { it == Cell.PAPER }
  }

  private fun getAdjacentPaperCount(grid: Grid<Cell>, point: Point): Int =
    point.adjacents(Adjacency.ALL).count { neighbour -> grid[neighbour] == Cell.PAPER }

  private fun findPapersToRemove(grid: Grid<Cell>): Set<Point> =
    grid.indicesOf(Cell.PAPER).filter { point ->
      getAdjacentPaperCount(grid, point) < MIN_ADJACENT_TO_STAY
    }.toSet()

  private fun removePapers(grid: Grid<Cell>, papersToRemove: Set<Point>): Grid<Cell> =
    grid.mapIndexed { point, cell ->
      if (point in papersToRemove) Cell.EMPTY else cell
    }
}

fun main() = solution.run()
