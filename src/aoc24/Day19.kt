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
    input.designs.count { it.makeDesign(input.patterns) > 0 }.toLong()

  override fun part2(input: Input): Output = input.designs.sumOf { it.makeDesign(input.patterns) }

  private fun String.makeDesign(
    patterns: List<String>,
    cache: MutableMap<String, Long> = mutableMapOf<String, Long>(),
  ): Long {
    if (isEmpty()) return 1
    return cache.getOrPut(this) {
      patterns.sumOf { pattern ->
        if (this.startsWith(pattern)) removePrefix(pattern).makeDesign(patterns, cache)
        else 0
      }
    }
  }
}

fun main() = solution.run()
