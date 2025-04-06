@file:Suppress("PackageDirectoryMismatch")

package aoc21.day14

import lib.Collections.histogram
import lib.Solution

private typealias Rules = Map<String, List<String>>

private data class Input(val template: String, val rules: Rules) {
  companion object {
    fun parse(input: String): Input {
      val (templateStr, rulesStr) = input.split("\n\n")
      val template = templateStr.trim()
      val rules = rulesStr.lines().associate { line ->
        val (pair, insert) = line.split(" -> ")
        check(pair.length == 2) { "Invalid pair: $pair" }
        pair to listOf(pair[0] + insert, insert + pair[1])
      }
      return Input(template, rules)
    }
  }
}

private typealias Output = Long

private val solution = object : Solution<Input, Output>(2021, "Day14") {
  override fun parse(input: String): Input = Input.parse(input)

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output {
    val repeats = when (part) {
      Part.PART1 -> 10
      Part.PART2 -> 40
    }

    var pairs: Map<String, Long> = patternToPairs(input.template)
    repeat(repeats) {
      pairs = pairs.expand(input.rules)
    }

    val counts: Map<Char, Long> = pairsToCounts(pairs, input.template[0])
    return counts.values.max() - counts.values.min()
  }

  private fun Map<String, Long>.expand(rules: Rules): Map<String, Long> =
    entries.flatMap { (pair, count) ->
      rules.getOrDefault(pair, listOf(pair)).map { it to count }
    }.groupBy({ it.first }, { it.second }).mapValues { it.value.sum() }

  private fun patternToPairs(pattern: String): Map<String, Long> =
    pattern.windowed(2).histogram().mapValues { it.value.toLong() }

  private fun pairsToCounts(pairs: Map<String, Long>, first: Char): Map<Char, Long> {
    val charCounts = pairs.entries.map { (pair, count) ->
      pair[1] to count
    }
    val fixedCharCount = charCounts + listOf(first to 1L)
    return fixedCharCount
      .groupBy({ it.first }, { it.second })
      .mapValues { it.value.sum() }
  }
}

fun main() = solution.run()
