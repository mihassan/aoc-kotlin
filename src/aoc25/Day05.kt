@file:Suppress("PackageDirectoryMismatch")

package aoc25.day05

import lib.Solution

data class Input(
  val freshIngredients: List<LongRange>,
  val ingredients: List<Long>,
) {
  companion object {
    const val SECTION_DELIMITER = "\n\n"

    fun parse(inputStr: String): Input {
      val (freshIngredientsStr, ingredientsStr) = inputStr.split(SECTION_DELIMITER)
      val freshIngredients = freshIngredientsStr.lines().map { line ->
        val (start, end) = line.split("-").map { it.toLong() }
        start..end
      }
      val ingredients = ingredientsStr.lines().map { it.toLong() }
      return Input(freshIngredients, ingredients)
    }
  }
}

typealias Output = Long

private val solution = object : Solution<Input, Output>(2025, "Day05") {
  override fun parse(input: String): Input = Input.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.ingredients.count { ingredient ->
    input.freshIngredients.any { range -> ingredient in range }
  }.toLong()

  override fun part2(input: Input): Output =
    input.freshIngredients.mergeOverlapping().sumOf { it.last - it.first + 1 }

  /** Merges overlapping or adjacent ranges into consolidated ranges. */
  private fun List<LongRange>.mergeOverlapping(): List<LongRange> {
    val sortedRanges = sortedBy { it.first }
    val mergedRanges = mutableListOf<LongRange>()
    for (range in sortedRanges) {
      val last = mergedRanges.lastOrNull()
      if (last != null && range.first <= last.last + 1) {
        mergedRanges[mergedRanges.lastIndex] = last.first..maxOf(last.last, range.last)
      } else {
        mergedRanges += range
      }
    }
    return mergedRanges
  }
}

fun main() = solution.run()
