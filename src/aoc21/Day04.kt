@file:Suppress("PackageDirectoryMismatch")

package aoc21.day03

import lib.Collections.histogram
import lib.Collections.transposed
import lib.Solution

private data class BinaryNumber(val bits: List<Int>) {
  init {
    require(bits.all { it in 0..1 }) { "Invalid bits: $bits" }
  }

  fun toInt(): Int = bits.joinToString("").toInt(2)

  companion object {
    fun parse(binaryNumberStr: String): BinaryNumber =
      BinaryNumber(binaryNumberStr.map { it - '0' })
  }
}

private data class BinaryNumberList(private val numbers: List<BinaryNumber>) {
  private val size: Int = numbers.size
  private val bitCount: Int = numbers.first().bits.size

  private fun countBits(): List<Map<Int, Int>> =
    numbers.map { it.bits }.transposed().map { it.histogram() }

  fun gamma(): BinaryNumber =
    BinaryNumber(countBits().map { it.maxByOrNull { it.value }!!.key })

  fun epsilon(): BinaryNumber =
    BinaryNumber(countBits().map { it.minByOrNull { it.value }!!.key })

  fun filterByBitAtIndex(bitIndex: Int, predicate: (Map<Int, Int>) -> Int): BinaryNumberList {
    if (size <= 1 || bitIndex >= bitCount) return this

    val selectedBit = predicate(countBits()[bitIndex])
    val filteredNumbers = BinaryNumberList(numbers.filter { it.bits[bitIndex] == selectedBit })

    return filteredNumbers.filterByBitAtIndex(bitIndex + 1, predicate)
  }

  fun single(): BinaryNumber = numbers.single()
}

private sealed interface Rating {
  fun predicate(counts: Map<Int, Int>): Int

  fun getRating(numbers: BinaryNumberList): Int =
    numbers.filterByBitAtIndex(0, ::predicate).single().toInt()

  object O2 : Rating {
    override fun predicate(counts: Map<Int, Int>): Int =
      when {
        0 !in counts -> 1
        1 !in counts -> 0
        counts[0]!! > counts[1]!! -> 0
        else -> 1
      }
  }

  object CO2 : Rating {
    override fun predicate(counts: Map<Int, Int>): Int =
      when {
        0 !in counts -> 1
        1 !in counts -> 0
        counts[0]!! > counts[1]!! -> 1
        else -> 0
      }
  }
}

private typealias Input = BinaryNumberList

private typealias Output = Int

private val solution = object : Solution<Input, Output>(2021, "Day03") {
  override fun parse(input: String): Input =
    BinaryNumberList(input.lines().map(BinaryNumber::parse))

  override fun format(output: Output): String = output.toString()

  override fun part1(input: Input): Output =
    input.gamma().toInt() * input.epsilon().toInt()

  override fun part2(input: Input): Output =
    Rating.O2.getRating(input) * Rating.CO2.getRating(input)
}

fun main() = solution.run()
