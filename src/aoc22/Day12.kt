@file:Suppress("PackageDirectoryMismatch")

package aoc2022.day12

import lib.Grid
import lib.Point
import lib.Solution

enum class PlotType {
  START, END, MIDDLE
}

data class Plot(val plotType: PlotType, val height: Int) {
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

  override fun solve(part: Part, input: Input): Output {
    val end = input.indexOf { it.plotType == PlotType.END }

    return when (part) {
      // We are computing the shortest distance backward from END to START.
      Part.PART1 -> input.shortedDistance(end) { it.plotType == PlotType.START }
      Part.PART2 -> input.shortedDistance(end) { it.height == 0 }
    }
  }

  fun Grid<Plot>.canReachFrom(points: Pair<Point, Point>): Boolean =
    this[points.second].height <= this[points.first].height + 1

  fun Grid<Plot>.shortedDistance(start: Point, endPredicate: (Plot) -> Boolean): Int {
    val distance = mutableMapOf(start to 0)
    val queue = ArrayDeque(listOf(start))

    fun Point.isNotVisited(): Boolean = this !in distance

    fun stepBackwardFrom(points: Pair<Point, Point>) {
      distance[points.second] = distance[points.first]!! + 1
      queue.addLast(points.second)
    }

    while (queue.isNotEmpty()) {
      val currPoint = queue.removeFirst()

      adjacents(currPoint)
        .filter(Point::isNotVisited)
        // This is intentionally backward as we start from END.
        .filter { nextPoint -> canReachFrom(nextPoint to currPoint) }
        .forEach { nextPoint -> stepBackwardFrom(currPoint to nextPoint) }
    }
    return indicesOf(endPredicate).mapNotNull { distance[it] }.min()
  }
}

fun main() = solution.run()
