@file:Suppress("PackageDirectoryMismatch")

package aoc22.day12

import lib.Grid
import lib.Point
import lib.Solution

enum class PlotType {
  START, END, MIDDLE
}

data class Plot(val plotType: PlotType, val height: Int) {
  infix fun canStepTo(other: Plot): Boolean = other.height <= height + 1

  companion object {
    fun parse(ch: Char): Plot = when (ch) {
      'S' -> Plot(PlotType.START, 0)
      'E' -> Plot(PlotType.END, 25)
      else -> Plot(PlotType.MIDDLE, ch - 'a')
    }
  }
}

typealias Input = Grid<Plot>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2022, "Day12") {
  override fun parse(input: String): Input = Grid.parse(input).map(Plot.Companion::parse)

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output =
    when (part) {
      Part.PART1 -> input.shortedDistance { it.plotType == PlotType.START }
      Part.PART2 -> input.shortedDistance { it.height == 0 }
    }

  fun Grid<Plot>.shortedDistance(start: (Plot) -> Boolean): Int {
    val distance = mutableMapOf<Point, Int>()
    val queue = ArrayDeque<Point>()

    fun Point.isNotVisited(): Boolean = this !in distance

    fun canStepFrom(points: Pair<Point, Point>): Boolean =
      this[points.first]?.let { p1 ->
        this[points.second]?.let { p2 ->
          p1 canStepTo p2
        }
      } ?: false

    fun stepFrom(points: Pair<Point, Point>) {
      distance[points.second] = distance[points.first]!! + 1
      queue.add(points.second)
    }

    indicesOf(start).forEach {
      distance[it] = 0
      queue.add(it)
    }

    while (queue.isNotEmpty()) {
      val currPoint = queue.removeFirst()

      adjacents(currPoint)
        .filter { nextPoint -> nextPoint.isNotVisited() }
        .filter { nextPoint -> canStepFrom(currPoint to nextPoint) }
        .forEach { nextPoint ->
          stepFrom(currPoint to nextPoint)
          if (this[nextPoint]?.plotType == PlotType.END) {
            return distance[nextPoint]!!
          }
        }
    }

    error("Did not reach END.")
  }
}

fun main() = solution.run()
