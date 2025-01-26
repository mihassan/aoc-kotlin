@file:Suppress("PackageDirectoryMismatch")

package aoc24.day03

import lib.Solution

typealias Input = List<Instruction>

typealias Output = Int

sealed interface Instruction {
  data object DO : Instruction
  data object DON_T : Instruction
  data class MUL(val x: Int, val y: Int) : Instruction {
    val value: Int
      get() = x * y
  }
}

private val solution = object : Solution<Input, Output>(2024, "Day03") {
  private val INSTRUCTION_PATTERN = """mul\((\d+),(\d+)\)|do\(\)|don't\(\)""".toRegex()

  override fun parse(input: String): Input =
    INSTRUCTION_PATTERN.findAll(input).map {
      when (it.value) {
        "do()" ->
          Instruction.DO

        "don't()" ->
          Instruction.DON_T

        else -> {
          val (x, y) = it.destructured
          Instruction.MUL(x.toInt(), y.toInt())
        }
      }
    }.toList()

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input
      .filterIsInstance<Instruction.MUL>()
      .sumOf { it.value }

  override fun part2(input: Input): Output {
    var enabled = true
    var sum = 0

    input.forEach {
      when (it) {
        Instruction.DO -> enabled = true

        Instruction.DON_T -> enabled = false

        is Instruction.MUL -> if (enabled) sum += it.value
      }
    }

    return sum
  }
}

fun main() = solution.run()
