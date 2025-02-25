@file:Suppress("PackageDirectoryMismatch")

package aoc24.day18

import lib.Grid
import lib.Point
import lib.Solution

enum class Cell {
  SAFE, CORRUPTED;
}

typealias Input = List<Point>

typealias Output = List<Int>

private val solution = object : Solution<Input, Output>(2024, "Day18") {
  private val GRID_SIZE = 71

  override fun parse(input: String): Input {
    return input.lines().map { Point.parse(it) }
  }

  override fun format(output: Output): String = output.joinToString(",")

  override fun part1(input: Input): Output {
    val grid = createGridFromSomeCorruptedPoints(input, 1024)

    val start = Point(0, 0)
    val end = Point(GRID_SIZE - 1, GRID_SIZE - 1)

    val distance = calculateShortestDistance(grid, start)

    return distance[end]?.let { listOf(it) } ?: emptyList()
  }

  override fun part2(input: Input): Output {
    input.indices.forEach { inputSize ->
      val grid = createGridFromSomeCorruptedPoints(input, inputSize)

      val start = Point(0, 0)
      val end = Point(GRID_SIZE - 1, GRID_SIZE - 1)

      val distance = calculateShortestDistance(grid, start)

      if (distance[end] == null) {
        val (x, y) = input[inputSize - 1]
        return listOf(x, y)
      }
    }

    return emptyList()
  }


  private fun calculateShortestDistance(grid: Grid<Cell>, start: Point): MutableMap<Point, Int> {
    val distance = mutableMapOf(start to 0)
    val queue = ArrayDeque(listOf(start))

    while (queue.isNotEmpty()) {
      val currentCell = queue.removeFirst()
      val currentDistance = distance[currentCell]!!

      currentCell.adjacents()
        .filter { nextCell -> nextCell !in distance && grid[nextCell] == Cell.SAFE }
        .forEach { nextCell ->
          distance[nextCell] = currentDistance + 1
          queue += nextCell
        }
    }
    return distance
  }

  private fun createGridFromSomeCorruptedPoints(
    corruptedPoints: List<Point>, pointsCount: Int,
  ): Grid<Cell> {
    val someCorruptedPoints = corruptedPoints.take(pointsCount).toSet()
    val grid = List(GRID_SIZE) { y ->
      List(GRID_SIZE) { x ->
        if (Point(x, y) in someCorruptedPoints) Cell.CORRUPTED else Cell.SAFE
      }
    }
    return Grid(grid)
  }
}

fun main() = solution.run()
