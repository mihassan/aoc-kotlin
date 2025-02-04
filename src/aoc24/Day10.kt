@file:Suppress("PackageDirectoryMismatch")

package aoc24.day10

import lib.Grid
import lib.Point
import lib.Solution

typealias Input = Grid<Int>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2024, "Day10") {
  override fun parse(input: String): Input = Grid.parse(input).map { it.digitToInt() }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.indices().sumOf { input.score(it) }

  override fun part2(input: Input): Output = input.indices().sumOf { input.rating(it) }

  private fun Grid<Int>.score(point: Point): Int {
    if (get(point) != 0) return 0

    val visited = mutableSetOf<Point>()
    dfs(point, visited)

    return visited.map(::get).count { it == 9 }
  }

  private fun Grid<Int>.rating(point: Point): Int {
    if (get(point) != 0) return 0

    val visited = mutableSetOf<Point>()
    return dfs(point, visited)
  }

  private fun Grid<Int>.dfs(point: Point, visited: MutableSet<Point>): Int {
    visited.add(point)

    val currHeight = requireNotNull(get(point))
    if (currHeight == 9) return 1

    return point.adjacents().filter { get(it) == currHeight + 1 }.sumOf { dfs(it, visited) }
  }
}

fun main() = solution.run()
