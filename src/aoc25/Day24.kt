@file:Suppress("PackageDirectoryMismatch")

package aoc25.day24

import lib.Solution

typealias Input = List<String>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2025, "Day24") {
  override fun parse(input: String): Input = input.lines()

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    TODO("Implement part 1")
  }

  override fun part2(input: Input): Output {
    TODO("Implement part 2")
  }
}

fun main() = solution.run()
