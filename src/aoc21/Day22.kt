@file:Suppress("PackageDirectoryMismatch")

package aoc21.day22

import lib.Bag
import lib.Solution

private data class Point(val x: Int, val y: Int, val z: Int)

private data class Cuboid(
  val on: Boolean,
  val xRange: IntRange,
  val yRange: IntRange,
  val zRange: IntRange,
) {
  /**
   * Calculates the volume of the [Cuboid] regardless of whether it is on or off.
   */
  val volume: Long
    get() = xRange.count().toLong() * yRange.count() * zRange.count()

  /**
   * Calculates the signed volume of the [Cuboid].
   * If the [Cuboid] is "on", the volume is positive; if "off", it is negative.
   */
  val signedVolume: Long
    get() =
      when (on) {
        true -> volume
        false -> -volume
      }

  /**
   * Truncates the [Cuboid]'s ranges for each axis to the specified range.
   */
  fun truncate(range: IntRange): Cuboid =
    Cuboid(on, xRange.truncate(range), yRange.truncate(range), zRange.truncate(range))

  /**
   * Expands the [Cuboid] into a sequence of [Point]s representing all points within the cuboid.
   */
  fun expand(): Sequence<Point> = sequence {
    xRange.forEach { x ->
      yRange.forEach { y ->
        zRange.forEach { z ->
          yield(Point(x, y, z))
        }
      }
    }
  }

  /**
   * Intersects this [Cuboid] with another [Cuboid] and returns a new [Cuboid] representing the intersection.
   * If there is no intersection, returns an empty [Cuboid].
   * Whether the resulting [Cuboid] is "on" or "off" is determined by the original [Cuboid].
   * If original [Cuboid] was "on", the resulting cuboid will be "off" and vice versa.
   * This is useful for calculating the union of multiple [Cuboid]s.
   * As a consequence, the [intersect] method is not reflexive, meaning `a intersect b` is not the same as `b intersect a`.
   */
  infix fun intersect(other: Cuboid): Cuboid {
    val xOverlap = xRange intersect other.xRange
    val yOverlap = yRange intersect other.yRange
    val zOverlap = zRange intersect other.zRange

    return if (xOverlap.isEmpty() || yOverlap.isEmpty() || zOverlap.isEmpty()) {
      EMPTY
    } else {
      Cuboid(!on, xOverlap, yOverlap, zOverlap)
    }
  }

  companion object {
    private val CUBOID_REGEX =
      """(on|off) x=(-?\d+..-?\d+),y=(-?\d+..-?\d+),z=(-?\d+..-?\d+)""".toRegex()

    private val EMPTY = Cuboid(false, IntRange.EMPTY, IntRange.EMPTY, IntRange.EMPTY)

    fun parse(line: String): Cuboid {
      val match = CUBOID_REGEX.matchEntire(line) ?: error("Invalid cuboid line: $line")
      val (onOff, xRange, yRange, zRange) = match.destructured
      return Cuboid(onOff == "on", xRange.parseRange(), yRange.parseRange(), zRange.parseRange())
    }

    private fun IntRange.truncate(range: IntRange): IntRange =
      start.coerceAtLeast(range.start)..endInclusive.coerceAtMost(range.endInclusive)

    private infix fun IntRange.intersect(range: IntRange): IntRange =
      if (isEmpty() || range.isEmpty()) IntRange.EMPTY
      else (start.coerceAtLeast(range.start)..endInclusive.coerceAtMost(range.endInclusive))

    private fun String.parseRange(): IntRange {
      val (start, end) = split("..").map { it.toInt() }
      return start..end
    }
  }
}

private typealias Input = List<Cuboid>
private typealias Output = Long

private val solution = object : Solution<Input, Output>(2021, "Day22") {
  override fun parse(input: String): Input = input.lines().map(Cuboid::parse)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    val onCubes = mutableSetOf<Point>()
    input
      .map { it.truncate(-50..50) }
      .forEach { cuboid ->
        if (cuboid.on) {
          onCubes += cuboid.expand()
        } else {
          onCubes -= cuboid.expand()
        }
      }
    return onCubes.size.toLong()
  }

  override fun part2(input: Input): Output {
    // Using a Bag to keep track of the cuboids and their counts. This is more efficient than using
    // a List as there are many duplicate cuboids due to intersections. Also, Sets are not suitable
    // because we need to keep track of the count of each cuboid.
    val addedCuboids = Bag<Cuboid>()

    // Use inclusion-exclusion principle to calculate the total volume of "on" cuboids.
    // Start with an empty bag of cuboids.
    // For each cuboid in the input, we will add it to the bag and also calculate the intersections
    // with all existing cuboids in the bag.
    input.forEach { cuboid ->
      val cuboidsToAdd = Bag<Cuboid>()

      // Only add the cuboid if it is to be turned on.
      if (cuboid.on) cuboidsToAdd += cuboid

      // For each existing cuboid, calculate the intersection with the new cuboid.
      addedCuboids.entries.forEach { (existingCuboid, count) ->
        val intersection = existingCuboid intersect cuboid
        cuboidsToAdd += Bag.of(intersection to count)
      }

      addedCuboids += cuboidsToAdd
    }

    return addedCuboids.entries.entries.sumOf { it.key.signedVolume * it.value }
  }
}

fun main() = solution.run()
