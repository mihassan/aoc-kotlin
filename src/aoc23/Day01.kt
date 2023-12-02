@file:Suppress("PackageDirectoryMismatch")

package aoc23.day01

import lib.Solution

typealias Input = List<String>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2023, "Day01") {
  override fun parse(input: String): Input = input.lines()

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.sumOf { line ->
    val firstDigit = line.firstOrNull { it.isDigit() } ?: error("No first digit")
    val lastDigit = line.lastOrNull { it.isDigit() } ?: error("No last digit")
    "${firstDigit}${lastDigit}".toInt()
  }

  override fun part2(input: Input): Output = input.sumOf { line ->
    val firstDigit = numbers.findFirst(line)?.wordToInt() ?: error("No first digit")
    val lastDigit = numbers.findLast(line)?.wordToInt() ?: error("No last digit")
    "${firstDigit}${lastDigit}".toInt()
  }

  private fun Regex.findFirst(input: String): String? =
    this.find(input)?.value

  private fun Regex.findLast(input: String): String? =
    this.pattern.reversed().toRegex().find(input.reversed())?.value?.reversed()

  private fun String.wordToInt(): Int? = wordToIntMap[this] ?: toIntOrNull()

  private val numbers =
    """0|1|2|3|4|5|6|7|8|9|zero|one|two|three|four|five|six|seven|eight|nine""".toRegex()
  private val wordToIntMap = mapOf(
    "zero" to 0,
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
