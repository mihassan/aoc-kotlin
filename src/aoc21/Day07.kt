@file:Suppress("PackageDirectoryMismatch")

package aoc21.day07

import kotlin.math.abs
import lib.Maths.pow
import lib.Solution

private typealias Input = List<Int>

private typealias Output = Int

private val solution = object : Solution<Input, Output>(2021, "Day07") {
  override fun parse(input: String): Input = input.split(",").map(String::toInt)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.extents().minOf { target ->
      input.sumOf { constantFuelCost(it, target) }
    }

  override fun part2(input: Input): Output =
    input.extents().minOf { target ->
      input.sumOf { increasingFuelCost(it, target) }
    }

  private fun List<Int>.extents(): IntRange = min()..max()

  private fun constantFuelCost(a: Int, b: Int) = abs(a - b)

  private fun increasingFuelCost(a: Int, b: Int) = (((a - b) pow 2) + abs(a - b)) / 2
}

fun main() = solution.run()
