@file:Suppress("PackageDirectoryMismatch")

package aoc24.day17

import lib.Maths.pow
import lib.Solution

enum class Register(val operand: Long) {
  A(4), B(5), C(6);

  companion object {
    private val REGISTER_REGEX = Regex("Register (?<name>[A-C]): (?<value>\\d+)")

    fun parse(registerLine: String): Pair<Register, Long> =
      REGISTER_REGEX.matchEntire(registerLine)?.let {
        val (name, value) = it.destructured
        Register.valueOf(name) to value.toLong()
      } ?: error("Invalid register line")

    fun fromOperand(operand: Long): Register? =
      entries.firstOrNull { it.operand == operand }
  }
}

enum class OperandType {
  LITERAL, COMBO, IGNORED
}

enum class Instruction(val opcode: Long, val operandType: OperandType) {
  ADV(0, OperandType.COMBO),
  BXL(1, OperandType.LITERAL),
  BST(2, OperandType.COMBO),
  JNZ(3, OperandType.LITERAL),
  BXC(4, OperandType.IGNORED),
  OUT(5, OperandType.COMBO),
  BDV(6, OperandType.COMBO),
  CDV(7, OperandType.COMBO);

  fun fixOperand(operand: Long, registers: MutableMap<Register, Long>): Long =
    when (operandType) {
      OperandType.IGNORED -> 0L
      OperandType.LITERAL -> operand
      OperandType.COMBO -> {
        val register = Register.fromOperand(operand)
        if (register != null) {
          registers[register] ?: error("Value for $register is not provided")
        } else {
          operand
        }
      }
    }

  companion object {
    fun parse(opcode: Long): Instruction =
      Instruction.entries.firstOrNull { it.opcode == opcode } ?: error("Invalid opcode")
  }
}

data class Program(val code: List<Long>) {
  companion object {
    fun parse(programStr: String): Program =
      Program(programStr.substringAfter("Program: ").split(",").map { it.toLong() })
  }
}

data class Computer(val program: Program, val registers: MutableMap<Register, Long>) {
  private var ip = 0
  private val output = mutableListOf<Long>()

  // Resets the computer with the given registers where ip is set to 0 and output is cleared.
  // This is useful for running the same program with different inputs.
  fun reset(registers: Map<Register, Long>): Computer = Computer(program, registers.toMutableMap())

  fun halts() = ip !in 0..<program.code.lastIndex

  fun run(): List<Long> {
    while (!halts()) {
      step()
    }
    return output
  }

  fun step() {
    if (halts()) return
    val (opcode, rawOperand) = program.code[ip] to program.code[ip + 1]
    val instruction = Instruction.parse(opcode)
    val operand = instruction.fixOperand(rawOperand, registers)
    // println("ip=$ip, instruction=$instruction, operand=$operand, registers=$registers, output=$output")

    when (instruction) {
      Instruction.ADV -> {
        writeRegister(Register.A, readRegister(Register.A) / (2L pow operand))
        ip += 2
      }

      Instruction.BXL -> {
        writeRegister(Register.B, readRegister(Register.B) xor operand)
        ip += 2
      }

      Instruction.BST -> {
        writeRegister(Register.B, operand % 8)
        ip += 2
      }

      Instruction.JNZ -> {
        if (readRegister(Register.A) != 0L) {
          ip = operand.toInt()
        } else {
          ip += 2
        }
      }

      Instruction.BXC -> {
        writeRegister(Register.B, readRegister(Register.B) xor readRegister(Register.C))
        ip += 2
      }

      Instruction.OUT -> {
        output.add(operand % 8)
        ip += 2
      }

      Instruction.BDV -> {
        writeRegister(Register.B, readRegister(Register.A) / (2L pow operand))
        ip += 2
      }

      Instruction.CDV -> {
        writeRegister(Register.C, readRegister(Register.A) / (2L pow operand))
        ip += 2
      }
    }
  }

  private fun readRegister(register: Register): Long =
    registers[register] ?: error("Register $register is not initialized")

  private fun writeRegister(register: Register, value: Long) {
    registers[register] = value
  }

  companion object {
    fun parse(computerStr: String): Computer {
      val lines = computerStr.lines()
      val registers = lines.take(3).associate { Register.parse(it) }.toMutableMap()
      val program = Program.parse(lines.last())

      return Computer(program, registers)
    }
  }
}

typealias Input = Computer

typealias Output = List<Long>

private val solution = object : Solution<Input, Output>(2024, "Day17") {
  override fun parse(input: String): Input = Computer.parse(input)

  override fun format(output: Output): String = output.joinToString(",") { it.toString() }

  override fun part1(input: Input): Output = input.run()

  override fun part2(input: Input): Output {
    val target = input.program.code

    fun runWithA(a: Long): List<Long> =
      input.reset(mapOf(Register.A to a, Register.B to 0, Register.C to 0)).run()

    fun findA(highBits: Long): Long? {
      if (runWithA(highBits) == target)
        return highBits

      return (0..7).firstNotNullOfOrNull { lowBits ->
        val candidate = (highBits shl 3) + lowBits
        val output = runWithA(candidate)
        if (candidate > 0 && target.endsWith(output)) {
          findA(candidate)
        } else {
          null
        }
      }
    }

    return findA(0)
      ?.let { listOf(it) }
      ?: error("No solution found")
  }

  fun <A> List<A>.endsWith(other: List<A>): Boolean {
    if (size < other.size) return false
    return subList(size - other.size, size) == other
  }
}

fun main() = solution.run()
