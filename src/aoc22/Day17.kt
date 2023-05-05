@file:Suppress("PackageDirectoryMismatch")

package aoc22.day17

import aoc22.day17.Direction.Companion.move
import aoc22.day17.Direction.Companion.toDirection
import kotlin.math.ceil
import lib.Collections.histogram
import lib.Collections.repeat
import lib.Point
import lib.Solution
import lib.Ranges.contains

enum class Direction {
  LEFT, // Decrement x
  RIGHT, // Increment x
  DOWN; // Decrement x

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

enum class Rock(pattern: String) {
  ROCK__("####"), ROCK_X(
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

  // The y coordinate decreases as we go downward. As such, we need to reverse the rock patterns so
  // that last line has y = 0.
  val cells: Set<Point> = pattern.lines().reversed().flatMapIndexed { r, line ->
    line.withIndex().filter { it.value == '#' }.map { Point(it.index, r) }
  }.toSet()

  val boundingBox: Pair<IntRange, IntRange> = run {
    val xRange = cells.minOf { it.x }..cells.maxOf { it.x }
    val yRange = cells.minOf { it.y }..cells.maxOf { it.y }
    xRange to yRange
  }
}

data class FallingRock(private val rock: Rock, private var bottomLeft: Point) {
  private var lastBottomLeft: Point? = null

  fun getCells(): List<Point> = rock.cells.map { it + bottomLeft }

  fun getBoundingBox(): Pair<IntRange, IntRange> = run {
    val (xRange, yRange) = rock.boundingBox
    val newXRange = xRange.first + bottomLeft.x..xRange.last + bottomLeft.x
    val newYRange = yRange.first + bottomLeft.y..yRange.last + bottomLeft.y
    newXRange to newYRange
  }

  fun move(direction: Direction) {
    lastBottomLeft = bottomLeft
    bottomLeft = bottomLeft.move(direction)
  }

  fun undoMove() {
    lastBottomLeft?.let {
      bottomLeft = it
    }
  }
}

data class Chamber(private val grid: MutableSet<Point> = EMPTY_GRID.toMutableSet()) {
  fun add(rock: Rock): FallingRock = FallingRock(rock, Point(2, height() + 4))

  fun canPlace(rock: FallingRock): Boolean {
    val (xRange, yRange) = rock.getBoundingBox()
    if (yRange.any { it <= FLOOR_Y }) return false
    if (xRange !in BOUNDARY) return false
    return (rock.getCells() intersect grid).isEmpty()
  }

  fun place(rock: FallingRock) {
    grid += rock.getCells()
  }

  fun height(): Int = grid.maxOf { it.y }

  companion object {
    private val BOUNDARY = 0..6
    private const val FLOOR_Y = 0
    private val EMPTY_GRID = BOUNDARY.map { Point(it, FLOOR_Y) }.toSet()
  }
}

typealias Input = List<Direction>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2022, "Day17") {
  override fun parse(input: String): Input = input.map { it.toDirection() }

  override fun format(output: Output): String = "$output"

  private fun chamberHeights(input: Input): Sequence<Int> = sequence {
    val rockProducer = Rock.values().asSequence().repeat().iterator()
    val jetProducer = input.asSequence().repeat().iterator()

    val chamber = Chamber()
    yield(chamber.height())

    while (true) {
      val fallingRock = chamber.add(rockProducer.next())

      while (true) {
        fallingRock.move(jetProducer.next())
        if (!chamber.canPlace(fallingRock)) {
          fallingRock.undoMove()
        }

        fallingRock.move(Direction.DOWN)
        if (!chamber.canPlace(fallingRock)) {
          fallingRock.undoMove()
          break
        }
      }

      chamber.place(fallingRock)

      yield(chamber.height())
    }
  }

  override fun part1(input: Input): Output {
    return chamberHeights(input).elementAt(2022).toLong()
  }

  override fun part2(input: Input): Output {
    // We needed trial and error to find that 10000 rocks is enough to find a pattern.
    val chamberHeights = chamberHeights(input).take(10000).toList()
    val heightPatterns = chamberHeights
      .zipWithNext { a, b -> b - a }
      // Window step corresponds to number of rocks and size is a multiple of that.
      // Bigger the window size, less likely to find a false positive for a repeat.
      // Assuming at least 4 variations in height increase for a round of 5 rocks,
      // 10 rounds should be enough to detect unique repetition.
      .windowed(10 * Rock.values().size, Rock.values().size)
    val cycleLength = heightPatterns.mapIndexed { i, h ->
      i - heightPatterns.take(i).indexOfLast { h2 -> h == h2 }
    }.histogram().maxBy { it.value }.key * Rock.values().size
    val heightIncreasePerCycle =
      chamberHeights.takeLast(cycleLength + 1).run { last() - first() }
    val target = 1_000_000_000_000L
    val targetCycles = ceil((target - chamberHeights.size) / cycleLength.toDouble()).toLong()
    val baseHeight = chamberHeights.elementAt((target - targetCycles * cycleLength).toInt())
    return baseHeight + targetCycles * heightIncreasePerCycle
  }
}

fun main() = solution.run()
