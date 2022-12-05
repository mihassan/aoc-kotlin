@file:Suppress("PackageDirectoryMismatch")

package aoc21.day03

import lib.Solution
import lib.Collections.histogram

enum class Day03Rating {
  O2,
  CO2
}

private val solution = object : Solution<List<String>, Int>(2021, "Day03") {
  override fun parse(input: String): List<String> = input.lines()

  override fun format(output: Int): String = output.toString()

  fun countBits(input: List<String>): Map<Int, Map<Char, Int>> =
    input.flatMap { it.mapIndexed(::Pair) }
      .groupBy({ it.first }, { it.second })
      .mapValues { it.value.histogram() }

  fun gamma(input: Map<Int, Map<Char, Int>>) = input.mapValues {
    it.value.maxByOrNull { it.value }?.key ?: '0'
  }

  fun epsilon(input: Map<Int, Map<Char, Int>>) = input.mapValues {
    it.value.minByOrNull { it.value }?.key ?: '0'
  }

  fun Map<Int, Char>.readBits() = values.toCharArray().concatToString().toInt(2)

  override fun part1(input: List<String>) = countBits(input).let {
    gamma(it).readBits() * epsilon(it).readBits()
  }

  fun bitCriteria(bitCount: Map<Char, Int>, rating: Day03Rating): Char {
    return bitCount['1']?.let { one ->
      bitCount['0']?.let { zero ->
        when (rating) {
          Day03Rating.O2 -> if (one >= zero) '1' else '0'
          Day03Rating.CO2 -> if (one < zero) '1' else '0'
        }
      } ?: '1'
    } ?: '0'
  }

  tailrec fun filterValues(values: List<String>, rating: Day03Rating, bit: Int = 0): String {
    if (values.size == 1) {
      return values[0]
    }
    val bitCount = values.map { it[bit] }.histogram()
    val selectedBit = bitCriteria(bitCount, rating)
    return filterValues(values.filter { it[bit] == selectedBit }, rating, bit + 1)
  }

  override fun part2(input: List<String>) =
    filterValues(input, Day03Rating.O2).toInt(2) *
      filterValues(input, Day03Rating.CO2).toInt(2)
}

fun main() = solution.run()
