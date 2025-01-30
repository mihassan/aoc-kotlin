@file:Suppress("PackageDirectoryMismatch")

package aoc24.day07

import lib.Combinatorics.permutationsWithReplacement
import lib.Solution
import lib.Strings.longs

enum class Operator {
  PLUS, MULTIPLY, CONCAT;

  fun apply(op1: Long, op2: Long): Long =
    when (this) {
      PLUS -> op1 + op2
      MULTIPLY -> op1 * op2
      CONCAT -> "${op1}${op2}".toLong()
    }

  companion object {
    val BASIC_SET = setOf(PLUS, MULTIPLY)
    val EXTENDED_SET = Operator.values().toSet()
  }
}

data class Equation(val result: Long, val operands: List<Long>) {
  init {
    require(operands.isNotEmpty()) { "Operands can not be empty." }
  }

  fun test(operators: List<Operator>): Boolean = apply(operators) == result

  fun apply(operators: List<Operator>): Long {
    require(operands.size == operators.size + 1) {
      "The number of operators do not match with the number of operands."
    }

    var result = operands.first()

    operands.drop(1).zip(operators).forEach { (x, o) ->
      result = o.apply(result, x)
    }

    return result
  }

  companion object {
    fun parse(equationStr: String): Equation {
      val (resultPart, operandsPart) = equationStr.split(":")
      return Equation(resultPart.toLong(), operandsPart.longs())
    }
  }
}

typealias Input = List<Equation>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2024, "Day07") {
  override fun parse(input: String): Input = input.lines().map { Equation.parse(it) }

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output {
    val opsSet = when (part) {
      Part.PART1 -> Operator.BASIC_SET
      Part.PART2 -> Operator.EXTENDED_SET
    }

    return input.filter { eq ->
      val opsCount = eq.operands.size - 1
      permutationsWithReplacement(opsSet, opsCount).any(eq::test)
    }.sumOf { it.result }
  }
}

fun main() = solution.run()
