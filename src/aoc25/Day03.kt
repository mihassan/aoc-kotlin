@file:Suppress("PackageDirectoryMismatch")

package aoc25.day03

import lib.Solution

data class Bank(val batteries: List<Int>) {
  fun joltage(batteriesToTurnOn: Int): Long {
    var remainingBatteries = batteries
    var totalJoltage = 0L

    repeat(batteriesToTurnOn) { turn ->
      val searchRange = remainingBatteries.size - batteriesToTurnOn + turn + 1
      val (selectedIndex, selectedJoltage) = findMaxBatteryInRange(remainingBatteries, searchRange)

      check(selectedIndex >= 0) { "Not enough batteries to turn on" }

      totalJoltage = totalJoltage * 10 + selectedJoltage
      remainingBatteries = remainingBatteries.drop(selectedIndex + 1)
    }

    return totalJoltage
  }

  private fun findMaxBatteryInRange(batteries: List<Int>, range: Int): Pair<Int, Int> {
    var maxIndex = -1
    var maxValue = 0
    for (i in 0 until range) {
      if (batteries[i] > maxValue) {
        maxIndex = i
        maxValue = batteries[i]
      }
    }
    return maxIndex to maxValue
  }

  companion object {
    fun parse(bankStr: String): Bank = Bank(bankStr.map { it.digitToInt() })
  }
}

typealias Input = List<Bank>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2025, "Day03") {
  private val PART1_BATTERIES = 2
  private val PART2_BATTERIES = 12

  override fun parse(input: String): Input = input.lines().map(Bank.Companion::parse)

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output {
    val batteriesToTurnOn = when (part) {
      Part.PART1 -> PART1_BATTERIES
      Part.PART2 -> PART2_BATTERIES
    }
    return input.sumOf { bank -> bank.joltage(batteriesToTurnOn) }
  }
}

fun main() = solution.run()
