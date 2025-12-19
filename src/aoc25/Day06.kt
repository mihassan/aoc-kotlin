@file:Suppress("PackageDirectoryMismatch")

package aoc25.day06

import lib.Collections.transposed
import lib.Solution
import lib.Strings.words

enum class Operator(val symbol: Char) {
  ADD('+'), MULTIPLY('*');

  companion object {
    fun parse(operatorChar: Char): Operator =
      entries.find { it.symbol == operatorChar } ?: error("Unknown operator: $operatorChar")
  }
}

data class Equation(val operator: Operator, val operands: List<Long>) {
  fun evaluate(): Long = when (operator) {
    Operator.ADD -> operands.sum()
    Operator.MULTIPLY -> operands.reduce(Long::times)
  }

  companion object {
    fun parse(parts: List<String>): Equation {
      val operatorSymbol = parts.last().first()
      val operator = Operator.parse(operatorSymbol)
      val operands = parts.dropLast(1).map(String::toLong)
      return Equation(operator, operands)
    }
  }
}

typealias Input = String

typealias Output = Long

private val solution = object : Solution<Input, Output>(2025, "Day06") {
  override fun parse(input: String): Input = input

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input
      .lines()
      .map { it.words() }
      .transposed()
      .map(Equation::parse)
      .sumOf(Equation::evaluate)

  override fun part2(input: Input): Output =
    input
      .toColumns()
      .splitByBlanks()
      .map(::parseEquation)
      .sumOf(Equation::evaluate)

  private fun String.toColumns(): List<String> =
    lines()
      .normalizeLineLength()
      .map(String::toList)
      .transposed()
      .map { it.joinToString("") }

  private fun List<String>.splitByBlanks(): List<List<String>> =
    fold(mutableListOf(mutableListOf<String>())) { groups, line ->
      if (line.isBlank()) groups.add(mutableListOf())
      else groups.last().add(line)
      groups
    }

  private fun List<String>.normalizeLineLength(): List<String> =
    maxOf(String::length).let { maxLength -> map { it.padEnd(maxLength) } }

  private fun parseEquation(lines: List<String>): Equation {
    val operatorSymbol = lines.mapNotNull { it.last().takeUnless(Char::isWhitespace) }.single()
    val operands = lines.map { it.dropLast(1).trim().toLong() }
    return Equation(Operator.parse(operatorSymbol), operands)
  }
}

fun main() = solution.run()
