@file:Suppress("PackageDirectoryMismatch")

package aoc21.day09

import lib.Grid
import lib.Point
import lib.Solution

private typealias Input = Grid<Int>

private typealias Output = Int

private val solution = object : Solution<Input, Output>(2021, "Day09") {
  override fun parse(input: String): Input = Grid.parse(input).map(Char::digitToInt)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    return input.findLowestPoints().sumOf { input[it]!! + 1 }
  }

  override fun part2(input: Input): Output =
    input.findLowestPoints()
      .map { point -> input.findBasinSize(point) }
      .sorted()
      .takeLast(3)
      .reduce(Int::times)

  private fun Grid<Int>.findLowestPoints(): List<Point> =
    this.indices().filter { point -> isLowestPoint(point, get(point)!!) }

  private fun Grid<Int>.isLowestPoint(point: Point, pointHeight: Int): Boolean =
    point.adjacents().mapNotNull(::get).all { neighbourHeight -> neighbourHeight > pointHeight }

  private fun Grid<Int>.findBasinSize(point: Point): Int = findBasinAt(point).size

  private fun Grid<Int>.findBasinAt(point: Point): List<Point> {
    val visited = mutableSetOf<Point>()

    fun dfs(current: Point) {
      if (visited.contains(current) || get(current) == null || get(current) == 9) return
      visited.add(current)
      current.adjacents().forEach { neighbour -> dfs(neighbour) }
    }

    dfs(point)

    return visited.toList()
  }
}

fun main() = solution.run()
