@file:Suppress("PackageDirectoryMismatch")

package aoc22.day01

import lib.ProblemInput
import lib.Solution

private val solution = object : Solution<List<List<Int>>, Int>(2022, "Day01") {
  override fun parse(input: ProblemInput): List<List<Int>> = input.sectionsAs(ProblemInput::ints)

  override fun format(output: Int): String = output.toString()

  override fun part1(input: List<List<Int>>): Int = input.maxOf { it.sum() }

  override fun part2(input: List<List<Int>>): Int =
    input.map { it.sum() }.sorted().takeLast(3).sum()
}

fun main() = solution.run()
