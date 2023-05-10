@file:Suppress("PackageDirectoryMismatch")

package aoc22.day20

import lib.Solution
import lib.Strings.longs

typealias Input = List<Long>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2022, "Day20") {
  override fun parse(input: String): Input = input.longs()

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    val permutation = ArrayList(input.indices.toList())

    mixNumbers(input, permutation)

    return findGroveCoordinate(input, permutation)
  }

  override fun part2(input: Input): Output {
    val decryptedInput = input.map { it * 811589153 }
    val permutation = ArrayList(decryptedInput.indices.toList())

    repeat(10) {
      mixNumbers(decryptedInput, permutation)
    }

    return findGroveCoordinate(decryptedInput, permutation)
  }

  private fun mixNumbers(input: Input, permutation: ArrayList<Int>) {
    input.forEachIndexed { idx, x ->
      val curIdx = permutation.indexOf(idx)
      val newIdx = (curIdx + x).mod(input.size - 1)

      permutation.removeAt(curIdx)
      permutation.add(newIdx, idx)
    }
  }

  private fun findGroveCoordinate(input: Input, permutation: ArrayList<Int>): Long {
    val zero = permutation.indexOf(input.indexOf(0))
    return listOf(1000, 2000, 3000).sumOf {
      val idx = (zero + it).mod(input.size)
      input[permutation[idx]]
    }
  }
}

fun main() = solution.run()
