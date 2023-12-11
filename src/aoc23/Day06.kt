@file:Suppress("PackageDirectoryMismatch")

package aoc23.day06

import kotlin.math.ceil
import kotlin.math.sqrt
import lib.Maths.isOdd
import lib.Solution
import lib.Strings.extractLongs

data class Race(val time: Long, val distance: Long) {
  fun waysToWin(): Long {
    val discriminant = sqrt(1.0 * time * time - 4 * distance) / 2
    return when {
      time.isOdd() -> 2 * ceil(discriminant - 0.5).toLong()
      else -> 2 * ceil(discriminant).toLong() - 1
    }
  }
}

typealias Input = List<Race>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2023, "Day06") {
  override fun parse(input: String): Input =
    input.lines().map { it.extractLongs() }.let { (times, distances) ->
      times.zip(distances).map { (time, distance) -> Race(time, distance) }
    }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.map { it.waysToWin() }.fold(1, Long::times)

  override fun part2(input: Input): Output {
    val time = input.map { it.time }.joinToString(separator = "").toLong()
    val distance = input.map { it.distance }.joinToString(separator = "").toLong()

    return Race(time, distance).waysToWin()
  }
}

fun main() = solution.run()
