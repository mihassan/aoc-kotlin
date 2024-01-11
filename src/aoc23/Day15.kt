@file:Suppress("PackageDirectoryMismatch")

package aoc23.day15

import lib.Solution

sealed interface Operation {
  data object REMOVE : Operation
  data class REPLACE(val focalLength: Int) : Operation

  companion object {
    fun parse(opStr: String): Operation {
      return when (opStr.first()) {
        '-' -> REMOVE
        '=' -> REPLACE(opStr.drop(1).toInt())
        else -> error("Invalid operation: $opStr")
      }
    }
  }
}

data class Instruction(val raw: String, val label: String, val operation: Operation) {
  companion object {
    private val REGEX = Regex("""(\w+)([-=]\d*)""")

    fun parse(instructionStr: String): Instruction {
      val (label, opStr) = REGEX.matchEntire(instructionStr)?.destructured
        ?: error("Invalid instruction: $instructionStr")
      return Instruction(instructionStr, label, Operation.parse(opStr))
    }
  }
}

data class Lens(val label: String, val focalLength: Int)

data class Box(val lenses: MutableList<Lens> = mutableListOf()) {
  fun runOperation(instruction: Instruction) {
    when (instruction.operation) {
      Operation.REMOVE -> remove(instruction.label)
      is Operation.REPLACE -> replace(instruction.label, instruction.operation.focalLength)
    }
  }

  private fun remove(label: String) {
    lenses.removeIf { it.label == label }
  }

  private fun replace(label: String, focalLength: Int) {
    val lens = Lens(label, focalLength)
    val idx = lenses.indexOfFirst { it.label == label }
    if (idx == -1) {
      lenses.add(lens)
    } else {
      lenses[idx] = lens
    }
  }
}

typealias Input = List<Instruction>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2023, "Day15") {
  override fun parse(input: String): Input = input.split(",").map { Instruction.parse(it) }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.sumOf { runHash(it.raw) }

  override fun part2(input: Input): Output {
    val boxes = List(256) { Box() }

    input.forEach { boxes[runHash(it.label)].runOperation(it) }

    return boxes.withIndex().sumOf { (boxIdx, box) ->
      box.lenses.withIndex().sumOf { (lensIdx, lens) ->
        (boxIdx + 1) * (lensIdx + 1) * lens.focalLength
      }
    }
  }

  private fun runHash(input: String): Int =
    input.fold(0) { curr, ch -> (curr + ch.code) * 17 % 256 }
}

fun main() = solution.run()
