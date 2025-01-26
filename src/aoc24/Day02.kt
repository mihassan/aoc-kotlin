@file:Suppress("PackageDirectoryMismatch")

package aoc24.day02

import kotlin.math.abs
import lib.Solution
import lib.Strings.ints

typealias Input = List<List<Int>>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2024, "Day02") {
  override fun parse(input: String): Input = input.lines().map { it.ints() }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.count { it.isSafe() }

  override fun part2(input: Input): Output = input.count {
    it.isSafe() || it.indices.any { idx -> it.removeAt(idx).isSafe() }
  }

  private fun List<Int>.isSafe(): Boolean =
    (isIncreasing() || isDecreasing()) && hasNoLargeStep()

  private fun List<Int>.isIncreasing(): Boolean = zipWithNext { x, y -> y > x }.all { it }

  private fun List<Int>.isDecreasing(): Boolean = zipWithNext { x, y -> y < x }.all { it }

  private fun List<Int>.hasNoLargeStep(): Boolean =
    zipWithNext { x, y -> abs(x - y) in 1..3 }.all { it }

  private fun List<Int>.removeAt(idx: Int): List<Int> = toMutableList().apply { removeAt(idx) }
}

fun main() = solution.run()
