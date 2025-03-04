@file:Suppress("PackageDirectoryMismatch")

package aoc24.day20

import lib.Grid
import lib.Point
import lib.Solution

enum class Tile {
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

data class Input(val grid: Grid<Tile>) {
  val start: Point = grid.indexOf(Tile.START)
  val end: Point = grid.indexOf(Tile.END)

  companion object {
    fun parse(gridStr: String): Input = Grid.parse(gridStr).map { Tile.parse(it) }.let { Input(it) }
  }
}

typealias Output = Int

private val solution = object : Solution<Input, Output>(2024, "Day20") {
  override fun parse(input: String): Input = Input.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    val distances = calculateShortestDistances(input.grid, input.start)
    val saves = input.grid.indicesOf(Tile.WALL).mapNotNull {
      val neighbourDistances = it.adjacents().mapNotNull { distances[it] }
      if (neighbourDistances.size < 2)
        null
      else
        (neighbourDistances.max() - neighbourDistances.min() - 2)
    }
    return saves.count { it >= 100 }
  }

  override fun part2(input: Input): Output {
    val distances = calculateShortestDistances(input.grid, input.end)
    var totalCheats = 0

    distances.forEach { (cheatStart, distanceFromCheatStart) ->
      distances.forEach { (cheatEnd, distanceFromCheatEnd) ->
        val cheatSize = cheatStart.manhattanDistance(cheatEnd)
        val save = (distanceFromCheatStart - distanceFromCheatEnd) - cheatSize
        if (save >= 100 && cheatSize >= 1 && cheatSize <= 20)
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
