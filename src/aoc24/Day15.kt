@file:Suppress("PackageDirectoryMismatch")

package aoc24.day15

import lib.Direction
import lib.Grid
import lib.Point
import lib.Solution

enum class Tile(val char: Char) {
  WALL('#'), EMPTY('.'), ROBOT('@'), BOX('O'), LEFT_BOX('['), RIGHT_BOX(']');

  companion object {
    fun parse(tileChar: Char): Tile =
      entries.firstOrNull { it.char == tileChar } ?: error("Unknown tile: $tileChar")
  }
}

fun Direction.Companion.parse(char: Char): Direction? = when (char) {
  '^' -> Direction.UP
  'v' -> Direction.DOWN
  '<' -> Direction.LEFT
  '>' -> Direction.RIGHT
  else -> null
}

fun Grid<Tile>.render(): String =
  map { it.char }.grid.joinToString("\n") { it.joinToString("") }

data class Warehouse(val grid: Grid<Tile>, val movements: List<Direction>) {
  fun robotPosition(): Point = grid.indexOf { it == Tile.ROBOT }

  fun widenTiles(): Warehouse {
    val wideGridStr = grid
      .render()
      .replace("#", "##")
      .replace("O", "[]")
      .replace(".", "..")
      .replace("@", "@.")
    return Grid.parse(wideGridStr).map { Tile.parse(it) }.let { copy(grid = it) }
  }

  fun moveAllTiles(positions: Set<Point>, direction: Direction): Warehouse? {
    if (positions.any { grid[it] == Tile.WALL }) return null

    val currPositions = positions.filter { grid[it] != Tile.EMPTY }
    var nextPositions = currPositions.map { pos ->  pos.move(direction) }.toSet()

    if (direction == Direction.UP || direction == Direction.DOWN) {
      nextPositions = nextPositions.flatMap { pos ->
        when (grid[pos]) {
          Tile.LEFT_BOX -> listOf(pos, pos.move(Direction.RIGHT))
          Tile.RIGHT_BOX -> listOf(pos, pos.move(Direction.LEFT))
          else -> listOf(pos)
        }
      }.toSet()
    }

    if (nextPositions.isEmpty()) return this

    val warehouse = moveAllTiles(nextPositions, direction) ?: return null

    return currPositions.fold(warehouse) { acc, currPosition ->
      acc.moveSingleTile(currPosition, direction) ?: return null
    }
  }

  private fun moveSingleTile(currPosition: Point, movement: Direction): Warehouse? {
    val currTile = grid[currPosition] ?: return null

    val newPosition = currPosition.move(movement)
    if (grid[newPosition] != Tile.EMPTY) return null

    var newGrid = grid.set(currPosition, Tile.EMPTY).set(newPosition, currTile)
    return copy(grid = newGrid)
  }

  fun gpsCoordinates(): List<Int> =
    grid.indicesOf { it == Tile.BOX || it == Tile.LEFT_BOX }.map { (x, y) -> 100 * y + x }

  companion object {
    fun parse(input: String): Warehouse {
      val (gridPart, movementsPart) = input.trim().split("\n\n")
      val grid = Grid.parse(gridPart).map { Tile.parse(it) }
      val movements = movementsPart.mapNotNull { Direction.parse(it) }
      return Warehouse(grid, movements)
    }
  }
}

typealias Input = Warehouse

typealias Output = Int

private val solution = object : Solution<Input, Output>(2024, "Day15") {
  override fun parse(input: String): Input = Input.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    return input.movements.fold(input) { warehouse, movement ->
      warehouse.moveAllTiles(
        setOf(warehouse.robotPosition()), movement
      ) ?: warehouse
    }.gpsCoordinates().sum()
  }

  override fun part2(input: Input): Output = part1(input.widenTiles())
}

fun main() = solution.run()

