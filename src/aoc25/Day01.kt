@file:Suppress("PackageDirectoryMismatch")

package aoc25.day01

import kotlin.math.ceil
import kotlin.math.floor
import lib.Solution

typealias Input = List<Int>

typealias Output = Int

private val solution =
  object : Solution<Input, Output>(2025, "Day01") {
    override fun parse(input: String): Input =
      input.lines().map {
        when (it.first()) {
          'L' -> -it.drop(1).toInt()
          'R' -> it.drop(1).toInt()
          else -> error("Invalid input")
        }
      }

    override fun format(output: Output): String = "$output"

    override fun part1(input: Input): Output {
      var currentDialPosition = 50
      return input.count { turn ->
        currentDialPosition = (currentDialPosition + turn).mod(100)
        currentDialPosition == 0
      }
    }

    override fun part2(input: Input): Output {
      var currentDialPosition = 50
      return input.sumOf { turn ->
        val newDialPosition = currentDialPosition + turn
        val numberOfZerosCrossed =
          if (turn > 0) {
            floor(newDialPosition / 100.0).toInt()
          } else {
            // If currentDialPosition is 0, it means we just crossed 0. So, we need to subtract 1
            // from the ceil value to account for the current 0.
            (if (currentDialPosition == 0) 0 else 1) -
              ceil(newDialPosition / 100.0).toInt()
          }
        currentDialPosition = newDialPosition.mod(100)
        numberOfZerosCrossed
      }
    }
  }

fun main() = solution.run()
