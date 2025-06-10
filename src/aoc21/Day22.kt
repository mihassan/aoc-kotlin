@file:Suppress("PackageDirectoryMismatch")

package aoc21.day22

import lib.Solution

private data class Cuboid(
  val xRange: IntRange,
  val yRange: IntRange,
  val zRange: IntRange,
) {
  /**
   * Checks if the [Cuboid] is empty, meaning it has no volume in any dimension.
   */
  val isEmpty: Boolean
    get() = xRange.isEmpty() || yRange.isEmpty() || zRange.isEmpty()

  /**
   * Calculates the volume of the [Cuboid] regardless of whether it is on or off.
   */
  val volume: Long
    get() = xRange.count().toLong() * yRange.count() * zRange.count()

  /**
   * Truncates the [Cuboid]'s ranges for each axis to the specified range.
   */
  infix fun truncateTo(range: IntRange): Cuboid =
    Cuboid(
      xRange truncateTo range,
      yRange truncateTo range,
      zRange truncateTo range
    )

  /**
   * Intersects this [Cuboid] with another [Cuboid] and returns a new [Cuboid] representing the intersection.
   * If there is no intersection, returns an empty [Cuboid].
   */
  infix fun intersect(other: Cuboid): Cuboid =
    Cuboid(
      xRange intersect other.xRange,
      yRange intersect other.yRange,
      zRange intersect other.zRange
    )

  companion object {
    private infix fun IntRange.truncateTo(range: IntRange): IntRange =
      start.coerceAtLeast(range.start)..endInclusive.coerceAtMost(range.endInclusive)

    private infix fun IntRange.intersect(range: IntRange): IntRange =
      start.coerceAtLeast(range.start)..endInclusive.coerceAtMost(range.endInclusive)
  }
}

private data class RebootStep(val onOff: Boolean, val cuboid: Cuboid) {
  infix fun truncateTo(range: IntRange): RebootStep = RebootStep(onOff, cuboid truncateTo range)

  companion object {
    private val REBOOT_STEP_REGEX =
      """(on|off) x=(-?\d+..-?\d+),y=(-?\d+..-?\d+),z=(-?\d+..-?\d+)""".toRegex()

    fun parse(line: String): RebootStep {
      val match = REBOOT_STEP_REGEX.matchEntire(line) ?: error("Invalid cuboid line: $line")
      val (onOff, xRange, yRange, zRange) = match.destructured
      return RebootStep(
        onOff == "on",
        Cuboid(xRange.parseRange(), yRange.parseRange(), zRange.parseRange())
      )
    }

    private fun String.parseRange(): IntRange {
      val (start, end) = split("..").map { it.toInt() }
      return start..end
    }
  }
}

private typealias Input = List<RebootStep>
private typealias Output = Long

private val solution = object : Solution<Input, Output>(2021, "Day22") {
  override fun parse(input: String): Input = input.lines().map(RebootStep::parse)

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output {
    val inputCuboids = when (part) {
      Part.PART1 -> input.map { it truncateTo -50..50 }
      Part.PART2 -> input
    }

    // Using a Map to keep track of the cuboids and their counts. This is more efficient than using
    // a List as there are many duplicate cuboids due to intersections. Also, Sets are not suitable
    // because we need to keep track of the count of each cuboid.
    val cuboidCounts = mutableMapOf<Cuboid, Int>()

    // Use inclusion-exclusion principle to calculate the total volume of "on" cuboids.
    // For each cuboid in the input, we will add it to the map and also calculate the intersections
    // with all existing cuboids in the map.
    inputCuboids.forEach { (shouldTurnOn, newCuboid) ->
      val newCuboids = mutableMapOf<Cuboid, Int>().withDefault { 0 }

      // Only add the cuboid if it is to be turned on.
      if (shouldTurnOn)
        newCuboids[newCuboid] = newCuboids.getValue(newCuboid) + 1

      // For each existing cuboid, calculate the intersection with the new cuboid.
      // If the intersection is non-empty, we add it to the new cuboids with a negative count.
      cuboidCounts.entries.forEach { (existingCuboid, count) ->
        val intersection = existingCuboid intersect newCuboid
        if (!intersection.isEmpty) {
          newCuboids[intersection] = newCuboids.getValue(intersection) - count
        }
      }

      // Now we merge the new cuboids into the main cuboidCounts map.
      newCuboids.forEach { (newCuboid, count) ->
        cuboidCounts.merge(newCuboid, count, Int::plus)
      }
    }

    // Finally, we calculate the total volume by summing the volumes of all cuboids multiplied by their counts.
    return cuboidCounts.entries.sumOf { (cuboid, count) -> cuboid.volume * count }
  }
}

fun main() = solution.run()
