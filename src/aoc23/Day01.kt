@file:Suppress("PackageDirectoryMismatch")

package aoc23.day01

import lib.Solution

typealias Input = List<String>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2023, "Day01") {
  override fun parse(input: String): Input {
    return input.lines()
  }

  override fun format(output: Output): String {
    return "$output"
  }

  override fun part1(input: Input): Output {
    return input.sumOf { line ->
      line.filter { it.isDigit() }.run { "${first()}${last()}".toInt() }
    }
  }

  override fun part2(input: Input): Output {
    return input.map {
      replacements.fold(it) { acc, (match, replacement) ->
        match.toRegex().replace(acc, replacement)
      }
    }.run { part1(this) }
  }

  private val replacements = listOf(
    "on(e|8)" to "1n$1",
    "tw(o|1)" to "2w$1",
    "thre(e|8)" to "3hre$1",
    "four" to "4our",
    "fiv(e|8)" to "5iv$1",
    "six" to "6ix",
    "seve(n|9)" to "7eve$1",
    "eigh(t|2|3)" to "8igh$1",
    "nin(e|8)" to "9in$1"
  )
}

fun main() = solution.run()
