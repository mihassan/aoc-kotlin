@file:Suppress("PackageDirectoryMismatch")

package aoc23.day13

import lib.Solution

typealias Pattern = List<String>

typealias Input = List<Pattern>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2023, "Day13") {
  override fun parse(input: String): Input = input.split("\n\n").map { it.lines() }

  override fun format(output: Output): String {
    return "$output"
  }

  override fun solve(part: Part, input: Input): Output {
    val requiredDiff = when (part) {
      Part.PART1 -> 0
      Part.PART2 -> 1
    }
    return input.sumOf {
      it.findVerticalMirror(requiredDiff) + 100 * it.findHorizontalMirror(requiredDiff)
    }
  }

  private fun Pattern.findHorizontalMirror(requiredDiff: Int): Int =
    (size - 1 downTo 0).firstOrNull { mirror ->
      (0..<mirror).sumOf { row ->
        countRowDiff(mirror, row)
      } == requiredDiff
    } ?: 0

  private fun Pattern.countRowDiff(mirror: Int, row: Int): Int =
    getOrNull(2 * mirror - row - 1)?.countStringDiff(get(row)) ?: 0

  private fun String.countStringDiff(other: String): Int =
    zip(other).sumOf { (x, y) -> x.countCharDiff(y) }

  private fun Char.countCharDiff(other: Char): Int = if (this == other) 0 else 1

  private fun Pattern.findVerticalMirror(requiredDiff: Int): Int =
    transposed().findHorizontalMirror(requiredDiff)

  private fun Pattern.transposed(): Pattern =
    first().indices.map { col -> map { it[col] }.joinToString("") }
}

fun main() = solution.run()
