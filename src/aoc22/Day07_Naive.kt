@file:Suppress("PackageDirectoryMismatch")

package aoc22.day07_naive

import lib.Collections.prefixes
import lib.Solution
import lib.Strings.isInt

typealias Input = List<String>
typealias Output = Int

private val solution = object : Solution<Input, Output>(2022, "Day07") {
  override fun parse(input: String): Input = input.lines()

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    val sizes = getDirSizes(input)
    return sizes.filter { it <= 100000 }.sum()
  }

  override fun part2(input: Input): Output {
    val sizes = getDirSizes(input)
    val minThreshold = sizes.max() - 40000000
    return sizes.filter { it >= minThreshold }.min()
  }

  private fun getDirSizes(input: Input): List<Int> {
    val currentPath = mutableListOf<String>()
    val dirSizes = mutableMapOf<List<String>, Int>().withDefault { 0 }

    fun processCdCommand(path: String) {
      if (path == "..")
        currentPath.removeLast()
      else
        currentPath.add(path)
    }

    fun processFileSize(fileSize: Int) {
      currentPath.prefixes().forEach { fullPath ->
        dirSizes[fullPath] = dirSizes.getValue(fullPath) + fileSize
      }
    }

    input.forEach { line ->
      val parts = line.split(" ")

      if (line.startsWith("$ cd"))
        processCdCommand(parts[2])
      else if (parts[0].isInt())
        processFileSize(parts[0].toInt())
    }

    return dirSizes.values.toList()
  }
}

fun main() = solution.run()
