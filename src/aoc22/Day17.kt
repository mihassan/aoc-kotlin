@file:Suppress("PackageDirectoryMismatch")

package aoc22.day17

import aoc22.day17.Direction.Companion.move
import aoc22.day17.Direction.Companion.toDirection
import lib.Collections.repeat
import lib.Point
import lib.Solution
import lib.Ranges.contains

enum class Rock(private val pattern: String) {
  ROCK__("####"),
  ROCK_X(
    """
      .#.
      ###
      .#.
    """.trimIndent()
  ),
  ROCK_J(
    """
      ..#
      ..#
      ###
    """.trimIndent()
  ),
  ROCK_I(
    """
      #
      #
      #
      #
    """.trimIndent()
  ),
  ROCK_O(
    """
      ##
      ##
    """.trimIndent()
  );

  val cells: Set<Point> =
    pattern.lines().reversed().flatMapIndexed { r, line ->
      line.withIndex().filter { it.value == '#' }.map { Point(it.index, r) }
    }.toSet()

  val boundingBox: Pair<IntRange, IntRange> = run {
    val xRange = cells.minOf { it.x }..cells.maxOf { it.x }
    val yRange = cells.minOf { it.y }..cells.maxOf { it.y }
    xRange to yRange
  }
}

enum class Direction {
  LEFT,
  RIGHT,
  DOWN;

  companion object {
    fun Char.toDirection(): Direction = when (this) {
      '<' -> LEFT
      '>' -> RIGHT
      'V' -> DOWN
      else -> error("Invalid jet pattern.")
    }

    fun Point.move(direction: Direction) = when (direction) {
      LEFT -> Point(x - 1, y)
      RIGHT -> Point(x + 1, y)
      DOWN -> Point(x, y - 1)
    }
  }
}

data class Chamber(private val grid: Set<Point> = EMPTY_GRID) {
  fun add(rock: Rock): FallingRock = FallingRock(rock, Point(2, height() + 4))

  fun canPlace(rock: FallingRock): Boolean {
    val (xRange, yRange) = rock.boundingBox
    if (yRange.any { it <= FLOOR })
      return false
    if (xRange !in BOUNDARY)
      return false
    return (rock.cells intersect grid).isEmpty()
  }

  fun place(rock: FallingRock): Chamber = Chamber(grid + rock.cells)

  fun height(): Int = grid.maxOf { it.y }

  companion object {
    private val BOUNDARY = 0..6
    private val FLOOR = 0
    private val EMPTY_GRID = BOUNDARY.map { Point(it, FLOOR) }.toSet()
  }
}

data class FallingRock(private val rock: Rock, private val bottomLeft: Point) {
  val cells: List<Point> = rock.cells.map { it + bottomLeft }

  val boundingBox: Pair<IntRange, IntRange> = run {
    val (xRange, yRange) = rock.boundingBox
    val newXRange = xRange.first + bottomLeft.x..xRange.last + bottomLeft.x
    val newYRange = yRange.first + bottomLeft.y..yRange.last + bottomLeft.y
    newXRange to newYRange
  }

  fun move(direction: Direction): FallingRock = FallingRock(rock, bottomLeft.move(direction))
}

typealias Input = List<Direction>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2022, "Day17") {
  override fun parse(input: String): Input = input.map { it.toDirection() }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    var chamber = Chamber()
    val rockProducer = Rock.values().asSequence().repeat().iterator()
    val jetProducer = input.asSequence().repeat().iterator()

    repeat(2022) {
      var fallingRock = chamber.add(rockProducer.next())

      while (true) {
        fallingRock.move(jetProducer.next())
          .takeIf { chamber.canPlace(it) }
          ?.let { fallingRock = it }

        fallingRock.move(Direction.DOWN)
          .takeIf { chamber.canPlace(it) }
          ?.let { fallingRock = it }
          ?: break
      }

      chamber = chamber.place(fallingRock)
    }

    return chamber.height()
  }

  override fun part2(input: Input): Output {
    TODO("Not yet implemented")
  }
}

fun main() = solution.run()
