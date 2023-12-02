@file:Suppress("PackageDirectoryMismatch")

package aoc23.day01

import lib.Solution

typealias Input = List<String>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2023, "Day01") {
  override fun parse(input: String): Input = input.lines()

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.sumOf { line ->
    line.filter {
      it.isDigit()
    }.run {
      "${first()}${last()}".toInt()
    }
  }

  override fun part2(input: Input): Output = input.sumOf { line ->
    val firstDigit = numbers.find(line)!!.value.let { numberToDigit[it] ?: it.toInt() }
    val lastDigit = numbers.findLast(line)!!.let { numberToDigit[it] ?: it.toInt() }
    10 * firstDigit + lastDigit
  }

  private fun Regex.findLast(input: String): String? =
    this.pattern.reversed().toRegex().find(input.reversed())?.value?.reversed()

  private val numbers =
    """1|2|3|4|5|6|7|8|9|one|two|three|four|five|six|seven|eight|nine""".toRegex()
  private val numberToDigit = mapOf(
    "one" to 1,
    "two" to 2,
    "three" to 3,
    "four" to 4,
    "five" to 5,
    "six" to 6,
    "seven" to 7,
    "eight" to 8,
    "nine" to 9
  )
}

fun main() = solution.run()
