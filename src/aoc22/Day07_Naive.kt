@file:Suppress("PackageDirectoryMismatch")

package aoc22.day07_naive

import lib.Collections.prefixes
import lib.Solution
import lib.Strings.isInt

typealias Input = List<String>
typealias Output = Int

private val solution = object : Solution<Input, Output>(2022, "Day07") {
  // Only match cd command and file listings with size. Ignore ls command and dir listings.
  val REGEX = """[$] cd (.+)|(\d+) .*""".toRegex()

  override fun parse(input: String): Input =
    input.lines().mapNotNull { line ->
      REGEX.matchEntire(line)?.destructured?.let { (cdPath, fileSize) ->
        cdPath + fileSize
      }
    }

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
    val dirSizes = mutableMapOf<List<String>, Int>()

    input.forEach {
      when {
        it.isInt() -> currentPath.prefixes().forEach { path ->
          dirSizes.compute(path) { _, prevSize ->
            (prevSize ?: 0) + it.toInt()
          }
        }
        it == ".." -> currentPath.removeLast()
        else -> currentPath.add(it)
      }
    }

    return dirSizes.values.toList()
  }
}

fun main() = solution.run()
