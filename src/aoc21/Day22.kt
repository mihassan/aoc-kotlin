@file:Suppress("PackageDirectoryMismatch")

package aoc21.day22

import aoc21.day22.Cuboid.Companion.splits
import lib.Solution

private data class Point(val x: Int, val y: Int, val z: Int)

private data class Cuboid(
  val on: Boolean,
  val xRange: IntRange,
  val yRange: IntRange,
  val zRange: IntRange,
) {
  fun expand(): List<Point> = buildList {
    for (x in xRange) {
      for (y in yRange) {
        for (z in zRange) {
          add(Point(x, y, z))
        }
      }
    }
  }

  fun volume(): Long = (xRange.count * yRange.count * zRange.count).coerceAtLeast(0L)

  fun truncate(range: IntRange): Cuboid =
    Cuboid(on, xRange.truncate(range), yRange.truncate(range), zRange.truncate(range))

  operator fun contains(other: Cuboid): Boolean =
    other.xRange in xRange && other.yRange in yRange && other.zRange in zRange

  companion object {
    fun parse(line: String): Cuboid {
      val match = CUBOID_REGEX.matchEntire(line) ?: error("Invalid cuboid line: $line")
      val (onOff, xRange, yRange, zRange) = match.destructured
      return Cuboid(onOff == "on", xRange.parseRange(), yRange.parseRange(), zRange.parseRange())
    }

    fun List<Cuboid>.splits(): Sequence<Cuboid> = sequence {
      val xSplits = splits { it.xRange }
      val ySplits = splits { it.yRange }
      val zSplits = splits { it.zRange }

      xSplits.forEach { x ->
        ySplits.forEach { y ->
          zSplits.forEach { z ->
            yield(Cuboid(true, x, y, z))
          }
        }
      }
    }

    private val IntRange.count: Long
      get() = endInclusive - start + 1L

    private fun IntRange.truncate(range: IntRange): IntRange =
      start.coerceAtLeast(range.start)..endInclusive.coerceAtMost(range.endInclusive)

    private operator fun IntRange.contains(other: IntRange): Boolean =
      other.start in this && other.endInclusive in this

    private fun List<Cuboid>.splits(selector: (Cuboid) -> IntRange): List<IntRange> {
      val starts = map(selector).flatMap { listOf(it.start, it.endInclusive + 1) }
      return starts.sorted().distinct().zipWithNext { start, end -> start until end }
    }

    private fun String.parseRange(): IntRange {
      val (start, end) = split("..").map { it.toInt() }
      return start..end
    }

    private val CUBOID_REGEX =
      """(on|off) x=(-?\d+..-?\d+),y=(-?\d+..-?\d+),z=(-?\d+..-?\d+)""".toRegex()
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

  override fun part2(input: Input): Output =
    input.splits().sumOf { c ->
      val lastOperation = input.lastOrNull { c in it }
      if (lastOperation?.on == true) {
        c.volume()
      } else {
        0L
      }
    }
}

fun main() = solution.run()
