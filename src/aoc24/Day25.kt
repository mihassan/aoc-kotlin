@file:Suppress("PackageDirectoryMismatch")

package aoc24.day25

import aoc24.day25.Schematic.Key
import aoc24.day25.Schematic.Lock
import lib.Solution

sealed class Schematic(val pins: List<Int>) {
  class Lock(pins: List<Int>) : Schematic(pins)
  class Key(pins: List<Int>) : Schematic(pins)

  companion object {
    fun parse(lockStr: String): Schematic {
      val rows = lockStr.lines()
      val isLock = rows.first().all { it == '#' }
      val pins = List(5) { pinIdx ->
        rows.count { it[pinIdx] == '#' } - 1
      }
      return when {
        isLock -> Lock(pins)
        else -> Key(pins)
      }
    }
  }
}

data class Input(val schematics: List<Schematic>) {
  companion object {
    fun parse(inputStr: String): Input = Input(inputStr.split("\n\n").map { Schematic.parse(it) })
  }
}

typealias Output = Int

private val solution = object : Solution<Input, Output>(2024, "Day25") {
  override fun parse(input: String): Input = Input.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.schematics.sumOf { lock ->
      input.schematics.count { key ->
        lock is Lock && key is Key && lock fits key
      }
    }

  // There is no part 2 for this day.
  override fun part2(input: Input): Output = 0

  infix fun Lock.fits(key: Key) =
    pins.zip(key.pins).all { (lockPin, keyPin) -> lockPin + keyPin <= 5 }
}

fun main() = solution.run()
