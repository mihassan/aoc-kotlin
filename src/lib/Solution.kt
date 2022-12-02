package lib

import java.io.File
import lib.Solution.Part.PART1
import lib.Solution.Part.PART2

abstract class Solution<P, R>(private val year: Int, private val fileName: String? = null) {
  enum class Part { PART1, PART2 }

  abstract fun parse(input: String): P
  abstract fun format(output: R): String

  open fun part1(input: P): R = solve(PART1, input)

  open fun part2(input: P): R = solve(PART2, input)

  open fun solve(part: Part, input: P): R = when (part) {
    PART1 -> part1(input)
    PART2 -> part2(input)
  }

  fun run() {
    val reader = fileName?.let {
      File("src/data/aoc${year % 100}/${it}.txt").reader()
    } ?: IO.reader

    val input = parse(reader.readText())

    println("Solution for part 1: ${format(part1(input))}")
    println("Solution for part 2: ${format(part2(input))}")
  }
}
