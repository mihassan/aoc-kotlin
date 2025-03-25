@file:Suppress("PackageDirectoryMismatch")

package aoc21.day01

import lib.Solution
import lib.Strings.ints

private typealias Input = List<Int>

private typealias Output = Int

private val solution = object : Solution<Input, Output>(2021, "Day01") {
  override fun parse(input: String): Input = input.ints()

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.zipWithNext().count { (a, b) -> b > a }

  override fun part2(input: Input): Output = input.windowed(3).map { it.sum() }.let(::part1)
}

fun main() = solution.run()
