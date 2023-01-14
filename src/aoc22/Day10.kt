@file:Suppress("PackageDirectoryMismatch")

package aoc22.day10

import lib.Solution

sealed interface Instruction {
  object Noop : Instruction
  data class AddX(val value: Int) : Instruction

  companion object {
    fun parse(line: String): Instruction = when {
      line.startsWith("noop") -> Noop
      line.startsWith("addx") -> {
        val value = line.substringAfter(" ").toInt()
        AddX(value)
      }
      else -> error("Invalid input")
    }
  }
}

typealias Signal = Int

typealias Cycle = Int

typealias SignalTimeSeries = MutableMap<Cycle, Signal>

data class CPU(
  private var signal: Int = 1,
  private var cycle: Int = 1,
  private var signalTimeSeries: SignalTimeSeries = mutableMapOf(),
) {
  private fun tick() {
    signalTimeSeries[cycle] = signal
    cycle += 1
  }

  private fun runSingleInstruction(instruction: Instruction) {
    when(instruction) {
      Instruction.Noop -> tick()
      is Instruction.AddX -> {
        tick()
        tick()
        signal += instruction.value
      }
    }
  }

  fun runInstructions(instructions: List<Instruction>) =
    instructions.forEach { runSingleInstruction(it) }

  fun getSignalAtCycle(cycle: Cycle): Signal =
    signalTimeSeries[cycle] ?: signal
}

typealias Input = List<Instruction>

typealias Output = String

private val solution = object : Solution<Input, Output>(2022, "Day10") {
  override fun parse(input: String): Input = input.lines().map { Instruction.parse(it) }

  override fun format(output: Output): String = output

  override fun part1(input: Input): Output {
    val cpu = CPU()
    cpu.runInstructions(input)
    return (20..220 step 40)
      .sumOf { it * cpu.getSignalAtCycle(it) }
      .toString()
  }

  override fun part2(input: Input): Output {
    val cpu = CPU()
    cpu.runInstructions(input)
    return (1..240).map {
      val register = cpu.getSignalAtCycle(it)
      val column = (it - 1) % 40
      if (register in (column - 1)..(column + 1)) '█' else ' '
    }
      .joinToString("")
      .chunked(40)
      .joinToString("\n", prefix = "\n")
  }
}

fun main() = solution.run()
