@file:Suppress("PackageDirectoryMismatch")

package aoc24.day20

import lib.Grid
import lib.Point
import lib.ProblemInput
import lib.Solution

private enum class Tile {
  EMPTY, WALL, START, END;

  companion object {
    fun parse(tileChar: Char): Tile = when (tileChar) {
      '.' -> EMPTY
      '#' -> WALL
      'S' -> START
      'E' -> END
      else -> throw IllegalArgumentException("Invalid tile: $tileChar")
    }
  }
}

private typealias Input = Grid<Tile>

private typealias Output = Int

private val solution = object : Solution<Input, Output>(2024, "Day20") {
  override fun parse(input: ProblemInput): Input = input.gridAs(Tile::parse)

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output {
    val allowedCheatSize = when (part) {
      Part.PART1 -> 2
      Part.PART2 -> 20
    }

    val endTile = input.indexOf(Tile.END)
    val distances = calculateShortestDistances(input, endTile)
    var totalCheats = 0

    distances.forEach { (cheatStart, distanceFromCheatStart) ->
      distances.forEach { (cheatEnd, distanceFromCheatEnd) ->
        val cheatSize = cheatStart.manhattanDistance(cheatEnd)
        val save = (distanceFromCheatStart - distanceFromCheatEnd) - cheatSize
        if (save >= 100 && cheatSize >= 1 && cheatSize <= allowedCheatSize)
          totalCheats++
      }
    }

    return totalCheats
  }

  private fun calculateShortestDistances(grid: Grid<Tile>, start: Point): Map<Point, Int> {
    val distance = mutableMapOf(start to 0)
    val queue = ArrayDeque<Point>().apply { add(start) }

    while (queue.isNotEmpty()) {
      val current = queue.removeFirst()

      for (neighbor in current.adjacents()) {
        if (neighbor !in grid || neighbor in distance || grid[neighbor] == Tile.WALL)
          continue
        distance[neighbor] = distance[current]!! + 1
        queue.add(neighbor)
      }
    }

    return distance
  }
}

fun main() = solution.run()
