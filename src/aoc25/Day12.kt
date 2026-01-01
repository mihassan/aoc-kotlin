@file:Suppress("PackageDirectoryMismatch")

package aoc25.day12

import lib.ProblemInput
import lib.Solution

private typealias Input = List<String>

private typealias Output = Int

private val solution = object : Solution<Input, Output>(2025, "Day12") {
  override fun parse(input: ProblemInput): Input = input.lines()

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    TODO("Implement part 1")
  }

  override fun part2(input: Input): Output {
    TODO("Implement part 2")
  }
}

fun main() = solution.run()
