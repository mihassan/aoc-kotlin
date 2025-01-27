@file:Suppress("PackageDirectoryMismatch")

package aoc24.day05

import java.util.Collections
import lib.Solution

data class Rule(val first: Int, val last: Int) {
  companion object {
    fun parse(ruleStr: String): Rule {
      val pages = ruleStr.split("|").map { it.toInt() }
      require(pages.size == 2) { "Each rule should have exactly 2 pages." }
      return Rule(pages.first(), pages.last())
    }
  }
}

data class Update(val pages: List<Int>) {
  private val pageCount: Int
    get() = pages.size

  fun isInRightOrder(rules: Set<Rule>): Boolean =
    getAllPairs().all { it.pairIsInRightOrder(rules) }

  fun middlePage(): Int = pages[pageCount / 2]

  fun fixUpdate(rules: Set<Rule>): Update =
    findAndFixSingleError(rules)?.fixUpdate(rules) ?: this

  private fun findAndFixSingleError(rules: Set<Rule>): Update? =
    findSingleError(rules)?.fixSingleError()

  private fun findSingleError(rules: Set<Rule>): Pair<Int, Int>? =
    getAllPairs().find { !it.pairIsInRightOrder(rules) }

  private fun Pair<Int, Int>.fixSingleError(): Update =
    Update(pages.toMutableList().apply { Collections.swap(this, first, second) })

  private fun getAllPairs(): List<Pair<Int, Int>> =
    (0..<pageCount - 1).flatMap { i ->
      (i + 1..<pageCount).map { j ->
        i to j
      }
    }

  private fun Pair<Int, Int>.pairIsInRightOrder(rules: Set<Rule>): Boolean =
    Rule(pages[second], pages[first]) !in rules

  companion object {
    fun parse(updateStr: String): Update = Update(updateStr.split(",").map { it.toInt() })
  }
}

data class Input(val rules: Set<Rule>, val updates: List<Update>)

typealias Output = Int

private val solution = object : Solution<Input, Output>(2024, "Day05") {
  override fun parse(input: String): Input {
    val (ruleStr, updateStr) = input.split("\n\n")
    val rules = ruleStr.lines().map { Rule.parse(it) }.toSet()
    val updates = updateStr.lines().map { Update.parse(it) }
    return Input(rules, updates)
  }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.updates
      .filter { it.isInRightOrder(input.rules) }
      .sumOf { it.middlePage() }

  override fun part2(input: Input): Output =
    input.updates
      .filterNot { it.isInRightOrder(input.rules) }
      .map { it.fixUpdate(input.rules) }
      .sumOf { it.middlePage() }
}

fun main() = solution.run()
