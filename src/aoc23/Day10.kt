@file:Suppress("PackageDirectoryMismatch")

package aoc23.day10

import lib.Direction
import lib.Direction.*
import lib.Grid
import lib.Point
import lib.Solution

enum class Tile(val symbol: Char, val connections: Set<Direction>) {
  GROUND('.', emptySet()),
  VERTICAL('|', setOf(UP, DOWN)),
  HORIZONTAL('-', setOf(LEFT, RIGHT)),
  NORTH_EAST('L', setOf(UP, RIGHT)),
  NORTH_WEST('J', setOf(UP, LEFT)),
  SOUTH_WEST('7', setOf(DOWN, LEFT)),
  SOUTH_EAST('F', setOf(DOWN, RIGHT)),
  START('S', setOf(UP, DOWN, LEFT, RIGHT));

  companion object {
    fun parse(symbol: Char): Tile {
      return values().find { it.symbol == symbol } ?: error("Unknown symbol: $symbol")
    }
  }
}

data class Field(val grid: Grid<Tile>) {
  private val start: Point by lazy { grid.indexOf(Tile.START) }

  fun findPipe(): List<Point> = buildList {
    var curr = connectedPoints(start).first()
    var prev = start

    add(prev)
    while (curr != start) {
      add(curr)
      val next = connectedPoints(curr).first { it != prev }
      prev = curr
      curr = next
    }
  }

  private fun connectedPoints(from: Point): List<Point> =
    possibleConnectedPoints(from).filter { next ->
      next in grid && from in possibleConnectedPoints(next)
    }

  private fun possibleConnectedPoints(from: Point): List<Point> =
    grid[from].connections.map(from::move)

  companion object {
    fun parse(input: String): Field = Field(Grid.parse(input).map { Tile.parse(it) })
  }
}

typealias Input = Field

typealias Output = Int

private val solution = object : Solution<Input, Output>(2023, "Day10") {
  override fun parse(input: String): Input = Field.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.findPipe().size / 2

  override fun part2(input: Input): Output {
    TODO("Not yet implemented")
  }
}

fun main() = solution.run()
