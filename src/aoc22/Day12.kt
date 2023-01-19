@file:Suppress("PackageDirectoryMismatch")

package aoc2022.day22

import lib.Grid
import lib.Maths.isZero
import lib.Point
import lib.Solution

data class Input(val grid: Grid<Int>, val start: Point, val end: Point)

typealias Output = Int

private val solution = object : Solution<Input, Output>(2022, "Day12") {
  override fun parse(input: String): Input {
    lateinit var start: Point
    lateinit var end: Point

    val grid = input.lines().mapIndexed { r, row ->
      row.mapIndexed { c, ch ->
        when (ch) {
          'S' -> 0.also { start = Point(c, r) }
          'E' -> 25.also { end = Point(c, r) }
          else -> ch - 'a'
        }
      }
    }

    return Input(Grid(grid), start, end)
  }

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output {
    val distance = mutableMapOf<Point, Int>().withDefault { Int.MAX_VALUE }
    val queue = ArrayDeque<Point>()

    input.grid.forEachIndexed { point, height ->
      when (part) {
        Part.PART1 -> {
          if (point == input.start) {
            distance[point] = 0
            queue.addLast(point)
          }
        }
        Part.PART2 -> {
          if (height.isZero()) {
            distance[point] = 0
            queue.addLast(point)
          }
        }
      }
    }

    while (queue.isNotEmpty()) {
      val curr = queue.removeFirst()
      input.grid.adjacents(curr).forEach {
        if ((it !in distance) && (input.grid[it] <= input.grid[curr] + 1)) {
          queue.addLast(it)
          distance[it] = distance[curr]!! + 1
        }
      }
    }

    return distance[input.end]!!
  }
}

fun main() = solution.run()
