@file:Suppress("PackageDirectoryMismatch")

package aoc24.day06

import lib.Direction
import lib.Grid
import lib.Point
import lib.Solution

enum class Tile {
  EMPTY, BLOCKED;

  companion object {
    fun parse(tileChar: Char): Tile = when (tileChar) {
      '#' -> BLOCKED
      // Everything except '#' including the starting position is considered EMPTY Tile.
      else -> EMPTY
    }
  }
}

data class Pose(val position: Point, val direction: Direction)

data class Input(val grid: Grid<Tile>, val pose: Pose)

typealias Output = Int

private val solution = object : Solution<Input, Output>(2024, "Day06") {
  override fun parse(input: String): Input {
    val inputGrid = Grid.parse(input)
    val grid = inputGrid.map { Tile.parse(it) }
    val pose = inputGrid.startingPose() ?: error("Starting position not found.")
    return Input(grid, pose)
  }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.grid
      .patrolUntilLeavingGridOrNull(input.pose)!!
      .map { it.position }
      .toSet()
      .size

  override fun part2(input: Input): Output =
    input.grid
      .indices()
      .count { newBlock ->
        input.grid.willGetStuck(input.pose, newBlock)
      }

  private fun Char.toDirection(): Direction? =
    when (this) {
      '>' -> Direction.RIGHT
      '<' -> Direction.LEFT
      'v' -> Direction.DOWN
      '^' -> Direction.UP
      else -> null
    }

  private fun Grid<Char>.startingPose(): Pose? {
    val position = indexOfOrNull { it.toDirection() != null } ?: return null
    val direction = this[position]?.toDirection() ?: return null

    return Pose(position, direction)
  }

  private fun Grid<Tile>.step(pose: Pose): Pose? {
    val nextPosition = pose.position.move(pose.direction)
    val nextTile = this[nextPosition] ?: return null

    return when (nextTile) {
      // If next tile is EMPTY, just move there.
      Tile.EMPTY -> Pose(nextPosition, pose.direction)

      // If next tile is blocked, turn right.
      Tile.BLOCKED -> Pose(pose.position, pose.direction.turnRight())
    }
  }

  private fun Grid<Tile>.patrolUntilLeavingGridOrNull(pose: Pose): Set<Pose>? {
    val visited = mutableSetOf<Pose>()
    var currPose: Pose? = pose

    while (currPose != null && currPose !in visited) {
      visited.add(currPose)
      currPose = step(currPose)
    }

    return visited.takeIf { currPose == null }
  }

  private fun Grid<Tile>.willGetStuck(pose: Pose, block: Point): Boolean =
    set(block, Tile.BLOCKED).patrolUntilLeavingGridOrNull(pose) == null
}

fun main() = solution.run()
