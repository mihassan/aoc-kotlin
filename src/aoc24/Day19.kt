@file:Suppress("PackageDirectoryMismatch")

package aoc24.day19

import lib.Solution

data class Input(val patterns: List<String>, val designs: List<String>) {
  companion object {
    fun parse(input: String): Input {
      val (patterns, designs) = input.split("\n\n")
      return Input(patterns.split(", "), designs.lines())
    }
  }
}

typealias Output = Long

private val solution = object : Solution<Input, Output>(2024, "Day19") {
  override fun parse(input: String): Input = Input.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.designs.count { it.canBeMadeWith(input.patterns) }.toLong()

  override fun part2(input: Input): Output =
    input.designs.sumOf { it.canBeMadeHowManyWaysWith(input.patterns) }

  private fun String.canBeMadeWith(patterns: List<String>): Boolean {
    val memo = mutableMapOf<String, Boolean>()

    fun String.recursivelyCanBeMadeWith(): Boolean {
      if (isEmpty()) return true
      if (this in memo) return memo[this]!!

      val result = patterns.any { pattern ->
        endsWith(pattern) && dropLast(pattern.length).recursivelyCanBeMadeWith()
      }

      memo[this] = result
      return result
    }

    return recursivelyCanBeMadeWith()
  }

  private fun String.canBeMadeHowManyWaysWith(patterns: List<String>): Long {
    val memo = mutableMapOf<String, Long>()

    fun String.recursivelyCanBeMadeHowManyWaysWith(): Long {
      if (isEmpty()) return 1
      if (this in memo) return memo[this]!!

      val result = patterns.sumOf { pattern ->
        if (endsWith(pattern)) dropLast(pattern.length).recursivelyCanBeMadeHowManyWaysWith()
        else 0
      }

      memo[this] = result
      return result
    }

    return recursivelyCanBeMadeHowManyWaysWith()
  }
}


fun main() = solution.run()
