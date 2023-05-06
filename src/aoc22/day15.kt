@file:Suppress("PackageDirectoryMismatch")

package aoc22.day15

import kotlin.math.abs
import lib.Point
import lib.Ranges.size
import lib.Ranges.overlaps
import lib.Ranges.intersect
import lib.Ranges.union
import lib.Solution
import lib.Strings.extractInts

data class Sensor(
  val position: Point,
  val closestBeacon: Point,
) {
  val closestBeaconDistance by lazy {
    position.distance(closestBeacon)
  }

  companion object {
    fun parse(line: String): Sensor {
      val (sx, sy, bx, by) = line.extractInts()
      return Sensor(Point(sx, sy), Point(bx, by))
    }
  }
}

typealias Input = List<Sensor>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2022, "Day15") {
  override fun parse(input: String): Input = input.lines().map { Sensor.parse(it) }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    calculateNoBeaconColumns(input, 2000000, false).sumOf { it.size }.toLong()

  override fun part2(input: Input): Output {
    val queryRange = 0..4000000
    val targetRow = queryRange.single { row ->
      calculateNoBeaconColumns(input, row, true)
        .map { it intersect queryRange }
        .sumOf { it.size } < queryRange.size
    }
    val row = calculateNoBeaconColumns(input, targetRow, true)
    val targetCol = queryRange.single { col -> row.all { col !in it } }
    return targetCol * 4000000L + targetRow
  }

  private fun calculateNoBeaconColumns(
    sensors: List<Sensor>,
    targetRow: Int,
    ignoreDetectedBeacons: Boolean,
  ): List<IntRange> =
    sensors
      .map { calculateNoBeaconColumns(it, targetRow, ignoreDetectedBeacons) }
      .filter { it.size > 0 }
      .sortedBy { it.first }
      .fold(ArrayDeque()) { acc, range ->
        when {
          overlapsWithLastRange(acc, range) -> acc.apply { addLast(removeLast() union range) }
          else -> acc.apply { addLast(range) }
        }
      }

  private fun calculateNoBeaconColumns(
    sensor: Sensor,
    targetRow: Int,
    ignoreDetectedBeacons: Boolean,
  ): IntRange {
    val center = Point(sensor.position.x, targetRow)
    val slack = sensor.closestBeaconDistance - sensor.position.distance(center)
    return when {
      slack < 0 -> IntRange.EMPTY
      ignoreDetectedBeacons || sensor.closestBeacon.y != targetRow -> (center.x - slack)..(center.x + slack)
      sensor.closestBeacon.x > center.x -> (center.x - slack) until center.x + slack
      else -> (center.x - slack + 1)..(center.x + slack)
    }
  }

  private fun overlapsWithLastRange(ranges: ArrayDeque<IntRange>, range: IntRange) =
    ranges.lastOrNull()?.let { it overlaps range } ?: false
}

private fun Point.distance(other: Point) = abs(x - other.x) + abs(y - other.y)

fun main() = solution.run()
