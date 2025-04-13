@file:Suppress("PackageDirectoryMismatch")

package aoc21.day17

import lib.Point
import lib.Solution

private data class Input(val left: Int, val right: Int, val bottom: Int, val top: Int) {
  val xRange = left..right
  val yRange = bottom..top

  operator fun contains(point: Point): Boolean {
    return point.x in xRange && point.y in yRange
  }
}
private typealias Output = Int

private val solution = object : Solution<Input, Output>(2021, "Day17") {
  override fun parse(input: String): Input {
    val regex = """target area: x=(-?\d+)..(-?\d+), y=(-?\d+)..(-?\d+)""".toRegex()
    val (x1, x2, y1, y2) = (regex.matchEntire(input) ?: error("Invalid input format")).destructured
    return Input(
      left = x1.toInt(),
      right = x2.toInt(),
      bottom = y1.toInt(),
      top = y2.toInt()
    )
  }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    val maxYDrop = - input.bottom
    val highestY = maxYDrop * (maxYDrop - 1) / 2
    return highestY
  }

  override fun part2(input: Input): Output {
    val xRange = 1..input.right
    val yRange = input.bottom..-input.bottom
    return xRange.sumOf { x ->
      yRange.count { y ->
        val initialSpeed = Point(x, y)
        trajectory(input, initialSpeed).any { it in input }
      }
    }
  }

  private fun trajectory(target: Input, initialSpeed: Point): Sequence<Point> = sequence {
    var pos = Point(0, 0)
    var speed = initialSpeed
    while (pos.x <= target.right && pos.y >= target.bottom) {
      yield(pos)
      pos = pos + speed
      speed = Point(
        x = if (speed.x > 0) speed.x - 1 else 0,
        y = speed.y - 1
      )
    }
  }
}

fun main() = solution.run()
