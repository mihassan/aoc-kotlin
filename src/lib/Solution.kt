package lib

import java.io.File
import lib.IO

abstract class Solution<P, R>(private val fileName: String? = null) {
  abstract fun parse(input: String): P
  abstract fun format(output: R): String

  abstract fun part1(input: P): R
  abstract fun part2(input: P): R

  fun run() {
    val reader = fileName?.let {
      File("src/data/${it}.txt").reader()
    } ?: IO.reader

    val input = parse(reader.readText())

    println("Solution for part 1: ${format(part1(input))}")
    println("Solution for part 2: ${format(part2(input))}")
  }
}
