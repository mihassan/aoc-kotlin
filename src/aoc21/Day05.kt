@file:Suppress("PackageDirectoryMismatch")

package aoc21.day05

import lib.Collections.histogram
import lib.Line
import lib.Solution

private typealias Input = List<Line>

private typealias Output = Int

private val solution = object : Solution<Input, Output>(2021, "Day05") {
  override fun parse(input: String): Input {
    return input.lines().map(Line.Companion::parse)
  }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input
      .filterNot { it.isDiagonal() }
      .flatMap { it.expand() }
      .histogram()
      .values
      .count { it > 1 }

  override fun part2(input: Input): Output =
    input
      .flatMap { it.expand() }
      .histogram()
      .values
      .count { it > 1 }

}

fun main() = solution.run()
