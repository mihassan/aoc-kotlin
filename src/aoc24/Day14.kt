@file:Suppress("PackageDirectoryMismatch")

package aoc24.day14

import lib.Point
import lib.Solution

typealias Vector = Point

enum class Quadrant {
  TopLeft, TopRight, BottomRight, BottomLeft
}

data class Robot(val position: Point, val velocity: Vector) {
  fun move(width: Int, height: Int, n: Int): Robot {
    val x = (position.x + velocity.x * n).mod(width)
    val y = (position.y + velocity.y * n).mod(height)
    return copy(position = Point(x, y))
  }

  companion object {
    private val ROBOT_REGEX =
      """p=(?<posX>-?\d+),(?<posY>-?\d+) v=(?<velX>-?\d+),(?<velY>-?\d+)""".toRegex()

    fun parse(robotStr: String): Robot {
      ROBOT_REGEX.matchEntire(robotStr)?.let { match ->
        val (posX, posY, velX, velY) = match.destructured
        return Robot(Point(posX.toInt(), posY.toInt()), Point(velX.toInt(), velY.toInt()))
      } ?: error("Invalid robot string: $robotStr")
    }
  }
}

data class Grid(val width: Int, val height: Int, val robots: List<Robot>) {
  init {
    require(robots.all { it.position.x in 0 until width && it.position.y in 0 until height })
  }

  fun move(n: Int): Grid =
    copy(robots = robots.map { it.move(width, height, n) })

  fun getQuadrant(point: Point): Quadrant? = when {
    point.x < width / 2 && point.y < height / 2 -> Quadrant.TopLeft
    point.x > width / 2 && point.y < height / 2 -> Quadrant.TopRight
    point.x < width / 2 && point.y > height / 2 -> Quadrant.BottomLeft
    point.x > width / 2 && point.y > height / 2 -> Quadrant.BottomRight
    else -> null
  }

  fun getQuadrant(robot: Robot): Quadrant? = getQuadrant(robot.position)

  fun getQuadrantCount(): Map<Quadrant, Int> =
    robots.mapNotNull { getQuadrant(it) }.groupingBy { it }.eachCount()

  fun possiblyATree(): Boolean = "##########" in display()

  fun display(): String {
    val grid = Array(height) { CharArray(width) { '.' } }
    robots.forEach { grid[it.position.y][it.position.x] = '#' }
    return grid.joinToString("\n") { it.joinToString("") }
  }
}

typealias Input = Grid

typealias Output = Int

private val solution = object : Solution<Input, Output>(2024, "Day14") {
  override fun parse(input: String): Input {
    return input.lines().map { Robot.parse(it) }.let { Grid(101, 103, it) }
  }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.move(100).getQuadrantCount().values.reduce(Int::times)

  override fun part2(input: Input): Output {
    var grid = input
    repeat(10403) {
      if (grid.possiblyATree()) {
        return it
      }
      grid = grid.move(1)
    }
    return -1
  }
}

fun main() = solution.run()
