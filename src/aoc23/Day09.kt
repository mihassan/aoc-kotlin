@file:Suppress("PackageDirectoryMismatch")

package aoc23.day09

import lib.Maths.isZero
import lib.Solution
import lib.Strings.longs

typealias Input = List<List<Long>>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2023, "Day09") {
  override fun parse(input: String): Input = input.lines().map { it.longs() }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.sumOf { it.nextNumber() }

  override fun part2(input: Input): Output = input.sumOf { it.prevNumber() }

  fun List<Long>.nextNumber(): Long =
    if (all { it.isZero() }) 0L
    else last() + zipWithNext { a, b -> b - a }.nextNumber()

  fun List<Long>.prevNumber(): Long =
    if (all { it.isZero() }) 0L
    else first() - zipWithNext { a, b -> b - a }.prevNumber()
}

fun main() = solution.run()
