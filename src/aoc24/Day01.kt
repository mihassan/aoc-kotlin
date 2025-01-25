@file:Suppress("PackageDirectoryMismatch")

package aoc24.day01

import kotlin.math.abs
import lib.Collections.histogram
import lib.Solution
import lib.Strings.ints

typealias Input = List<List<Int>>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2024, "Day01") {
  override fun parse(input: String): Input = input.lines().map { it.ints() }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    val l1 = input.map { it[0] }.sorted()
    val l2 = input.map { it[1] }.sorted()
    return l1.zip(l2).sumOf { (x, y) -> abs(x - y) }
  }

  override fun part2(input: Input): Output {
    val l = input.map { it[0] }
    val h = input.map { it[1] }.histogram()
    return l.sumOf { it * (h[it] ?: 0) }
  }
}

fun main() = solution.run()
