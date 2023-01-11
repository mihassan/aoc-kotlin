@file:Suppress("PackageDirectoryMismatch")

package aoc22.day10

import kotlin.math.abs
import lib.Solution

sealed interface Instruction {
  object Noop : Instruction
  data class AddX(val v: Int) : Instruction

  companion object {
    fun parse(line: String): Instruction = when {
      line.startsWith("noop") -> Noop
      line.startsWith("addx") -> AddX(line.substringAfter(" ").toInt())
      else -> error("Invalid input")
    }
  }
}

typealias Input = List<Instruction>

typealias Output = String

private val solution = object : Solution<Input, Output>(2022, "Day10") {
  override fun parse(input: String): Input = input.lines().map { Instruction.parse(it) }

  override fun format(output: Output): String = output

  override fun part1(input: Input): Output {
    val registerValues = run(input)
    return listOf(20, 60, 100, 140, 180, 220)
      .sumOf { it * registerValues.getRegisterValue(it) }
      .toString()
  }

  override fun part2(input: Input): Output {
    val registerValues = run(input)
    return (1..240).map {
      val register = registerValues.getRegisterValue(it)
      val column = (it - 1) % 40
      if (abs(register - column) <= 1) '#' else ' '
    }
      .joinToString("")
      .chunked(40)
      .joinToString("\n", prefix = "\n")
  }

  fun run(instructions: List<Instruction>): List<Int> {
    var x = 1
    return buildList {
      add(x)
      instructions.forEach {
        when (it) {
          Instruction.Noop -> add(x)
          is Instruction.AddX -> {
            add(x)
            x += it.v
            add(x)
          }
        }
      }
    }
  }

  fun List<Int>.getRegisterValue(cycle: Int) = getOrElse(cycle - 1) { last() }
}

fun main() = solution.run()

