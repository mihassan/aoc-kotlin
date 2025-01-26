@file:Suppress("PackageDirectoryMismatch")

package aoc24.day03

import lib.Solution

typealias Input = String

typealias Output = Int

private val solution = object : Solution<Input, Output>(2024, "Day03") {
  private val  MUL_PATTERN: Regex = """mul\((\d+),(\d+)\)""".toRegex()
  private val FULL_PATTERN= """mul\((\d+),(\d+)\)|do\(\)|don't\(\)""".toRegex()

  override fun parse(input: String): Input = input

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    return MUL_PATTERN
      .findAll(input)
      .map { it.destructured }
      .sumOf { (x, y) -> x.toInt() * y.toInt() }
  }

  override fun part2(input: Input): Output {
    var enabled = true
    var sum = 0
    FULL_PATTERN.findAll(input).forEach {
      when(it.value) {
        "do()" -> enabled = true
        "don't()" -> enabled = false
        else -> {
          if (enabled) {
            val (x, y) = it.destructured
            sum += x.toInt() * y.toInt()
          }
        }
      }
    }
    return sum
  }
}

fun main() = solution.run()
