@file:Suppress("PackageDirectoryMismatch")

package aoc22.day14

import lib.Path
import lib.Point
import lib.Solution

data class Cave(
  private val bricks: MutableSet<Point>,
  private val sands: MutableSet<Point>,
) {
  constructor(bricks: Set<Point> = emptySet()) : this(bricks.toMutableSet(), mutableSetOf())

  private var pitLevel: Int = bricks.maxOf { it.y }

  fun addBase() {
    pitLevel += 2
    bricks += (-pitLevel..pitLevel).map { Point(DROPPING_POINT.x + it, pitLevel) }
  }

  fun dropSand(): Boolean {
    if (DROPPING_POINT.isOccupied())
      return false

    var sand = DROPPING_POINT

    while (!sand.isStable()) {
      sand = sand.step()
      if (sand.isFallingToAbyss())
        return false
    }

    sands += sand

    return true
  }

  private fun Point.isStable() = potentialDropPoints().all { it.isOccupied() }

  private fun Point.isOccupied() = this in bricks || this in sands

  private fun Point.isFallingToAbyss() = y >= pitLevel

  private fun Point.step(): Point = potentialDropPoints().first { !it.isOccupied() }

  private fun Point.potentialDropPoints() = DROP_DIRS.map { dir -> this + dir }

  companion object {
    private val DROPPING_POINT = Point(500, 0)
    private val DROP_DIRS = listOf(Point(0, 1), Point(-1, 1), Point(1, 1))

    fun parse(caveStr: String): Cave {
      val bricks = caveStr.lines().map(Path.Companion::parse).flatMap(Path::expand).toSet()
      return Cave(bricks.toSet())
    }
  }
}

typealias Input = Cave

typealias Output = Int

private val solution = object : Solution<Input, Output>(2022, "Day14") {
  override fun parse(input: String): Input = Cave.parse(input)

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output {
    if (part == Part.PART2) input.addBase()

    var sand = 0
    while (input.dropSand()) sand++

    return sand
  }
}

fun main() = solution.run()
