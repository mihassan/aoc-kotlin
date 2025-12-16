@file:Suppress("PackageDirectoryMismatch")

package aoc25.day02

import lib.Solution

data class ProductIdRange(val start: Long, val end: Long) {
  val range: LongRange
    get() = start..end

  val digits: Int
    get() = "$end".length

  fun sumOfBasicInvalidIds(): Long =
    range.filter { it.hasRepeatingPattern(2) }.sum()

  fun sumOfExtendedInvalidIds(): Long =
    range.filter { n ->
      (2..digits).any { r ->
        n.hasRepeatingPattern(r)
      }
    }.sum()

  fun Long.hasRepeatingPattern(repetition: Int): Boolean {
    val numStr = "$this"
    val strLen = numStr.length
    if (strLen % repetition != 0) return false
    val patternLen = strLen / repetition
    if (patternLen == 0) return false
    return numStr.take(patternLen).repeat(repetition) == numStr
  }

  companion object {
    fun parse(rangeStr: String): ProductIdRange {
      val (start, end) = rangeStr.split("-").map { it.toLong() }
      return ProductIdRange(start, end)
    }
  }
}

typealias Input = List<ProductIdRange>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2025, "Day02") {
  override fun parse(input: String): Input = input.split(",").map(ProductIdRange.Companion::parse)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.sumOf { it.sumOfBasicInvalidIds() }

  override fun part2(input: Input): Output =
    input.sumOf { it.sumOfExtendedInvalidIds() }
}

fun main() = solution.run()
