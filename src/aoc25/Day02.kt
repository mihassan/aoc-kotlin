@file:Suppress("PackageDirectoryMismatch")

package aoc25.day02

import lib.Collections.allEqual
import lib.Maths.divides
import lib.ProblemInput
import lib.Solution
import lib.Strings.splitIn

private data class ProductIdRange(val start: Long, val end: Long) {
  val range: LongRange = start..end

  val digits: Int = end.toString().length

  fun sumOfBasicInvalidIds(): Long =
    range.filter { it.hasRepeatingPattern(2) }.sum()

  fun sumOfExtendedInvalidIds(): Long =
    range.filter { n -> (2..digits).any { n.hasRepeatingPattern(it) } }.sum()

  companion object {
    fun parse(rangeStr: String): ProductIdRange {
      val (start, end) = rangeStr.split("-").map(String::toLong)
      return ProductIdRange(start, end)
    }
  }
}

private fun Long.hasRepeatingPattern(repetition: Int): Boolean {
  val str = toString()
  if (!repetition.divides(str.length)) return false
  val parts = str.splitIn(repetition)
  return parts.size > 1 && parts.allEqual()
}

private typealias Input = List<ProductIdRange>

private typealias Output = Long

private val solution = object : Solution<Input, Output>(2025, "Day02") {
  override fun parse(input: ProblemInput): Input = input.commaSplitAs(ProductIdRange::parse)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.sumOf { it.sumOfBasicInvalidIds() }

  override fun part2(input: Input): Output = input.sumOf { it.sumOfExtendedInvalidIds() }
}

fun main() = solution.run()
