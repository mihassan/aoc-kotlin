@file:Suppress("PackageDirectoryMismatch")

package aoc24.day12

import aoc24.day12.Region.Companion.flatten
import lib.Direction
import lib.Grid
import lib.Point
import lib.Solution

data class Plot(val point: Point) {
  fun edges(): List<Edge> = Direction.values().map { direction -> Edge(this, direction) }

  fun adjacents(): List<Plot> = point.adjacents().map { adjacent -> Plot(adjacent) }

  fun move(direction: Direction): Plot = Plot(point.move(direction))
}

data class Edge(val plot: Plot, val direction: Direction) {
  fun otherPlot(): Plot = Plot(plot.point.move(direction))

  fun adjacents(): List<Edge> =
    listOf(direction.turnLeft(), direction.turnRight())
      .map { displacement ->
        Edge(plot.move(displacement), direction)
      }
}

data class Region(val plots: Set<Plot>) {
  fun getPrice(): Int = getArea() * getPerimeter()

  fun getBulkPrice(): Int = getArea() * getSides()

  operator fun plus(plot: Plot) = copy(plots = plots + plot)

  operator fun contains(plot: Plot) = plot in plots

  private fun getArea(): Int = plots.size

  private fun getPerimeter(): Int = getAllEdgesAtPerimeter().count()

  private fun getAllEdgesAtPerimeter(): List<Edge> =
    plots.flatMap { plot -> getPlotEdgesAtPerimeter(plot) }

  private fun getPlotEdgesAtPerimeter(plot: Plot): List<Edge> =
    plot.edges().filter { edge -> edge.otherPlot() !in plots }

  private fun getSides(): Int {
    var sides = 0
    val allEdges = getAllEdgesAtPerimeter().toSet()
    val visited = mutableSetOf<Edge>()

    allEdges.forEach { edge ->
      if (edge !in visited) {
        sides += 1
        visitEdgesWithCommonSide(edge, allEdges, visited)
      }
    }

    return sides
  }

  private fun visitEdgesWithCommonSide(edge: Edge, allEdges: Set<Edge>, visited: MutableSet<Edge>) {
    if (edge in visited) return
    visited += edge

    edge
      .adjacents()
      .filter { it in allEdges }
      .forEach { visitEdgesWithCommonSide(it, allEdges, visited) }
  }

  companion object {
    fun List<Region>.flatten(): Region = Region(map { it.plots }.flatten().toSet())
  }
}

typealias Input = Grid<Char>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2024, "Day12") {
  override fun parse(input: String): Input = Grid.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.findRegions().sumOf { region -> region.getPrice() }

  override fun part2(input: Input): Output =
    input.findRegions().sumOf { region -> region.getBulkPrice() }

  private fun Grid<Char>.findRegions(): List<Region> {
    val visited = mutableSetOf<Plot>()

    return indices().mapNotNull { point ->
      findRegionAt(Plot(point), visited)
    }
  }

  private fun Grid<Char>.findRegionAt(plot: Plot, visited: MutableSet<Plot>): Region? {
    val grid = this

    if (plot in visited) return null
    visited += plot

    return plot.adjacents().filter { adjacentPlot -> grid[adjacentPlot.point] == grid[plot.point] }
      .mapNotNull { adjacentPlot -> findRegionAt(adjacentPlot, visited) }.flatten() + plot
  }
}

fun main() = solution.run()
