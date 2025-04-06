@file:Suppress("PackageDirectoryMismatch")

package aoc21.day13

import kotlin.math.abs
import lib.Point
import lib.Solution

private typealias Grid = Set<Point>

private sealed interface Fold {
  data class X(val x: Int) : Fold
  data class Y(val y: Int) : Fold

  fun fold(point: Point): Point = when (this) {
    is X -> point.copy(x = x - abs(x - point.x))
    is Y -> point.copy(y = y - abs(y - point.y))
  }

  companion object {
    private val FOLD_REGEX = Regex("fold along ([xy])=(\\d+)")
    fun parse(foldStr: String): Fold {
      val match = FOLD_REGEX.matchEntire(foldStr) ?: error("Invalid fold string: $foldStr")
      val (axis, value) = match.destructured
      return when (axis) {
        "x" -> X(value.toInt())
        "y" -> Y(value.toInt())
        else -> error("Invalid axis: $axis")
      }
    }
  }
}

private data class Input(val grid: Grid, val folds: List<Fold>) {
  companion object {
    fun parse(input: String): Input {
      val (gridStr, foldStr) = input.split("\n\n")
      val grid = gridStr.lines().map(Point::parse).toSet()
      val folds = foldStr.lines().map(Fold::parse)
      return Input(grid, folds)
    }
  }
}

private typealias Output = Int

private val solution = object : Solution<Input, Output>(2021, "Day13") {
  override fun parse(input: String): Input = Input.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.grid.map { input.folds.first().fold(it) }.toSet().size

  override fun part2(input: Input): Output {
    var grid = input.grid
    for (fold in input.folds) {
      grid = grid.map { fold.fold(it) }.toSet()
    }
    grid.display()
    // The output is not returned, but printed to the console.
    return 0
  }

  private fun Grid.display() {
    val maxX = maxOf { it.x }
    val maxY = maxOf { it.y }
    for (y in 0..maxY) {
      for (x in 0..maxX) {
        print(if (Point(x, y) in this) "#" else ".")
      }
      println()
    }
  }
}

fun main() = solution.run()
