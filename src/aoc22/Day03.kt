package aoc22

import lib.Solution


private val solution = object : Solution<List<String>, Int>(2022, "Day03") {
  override fun parse(input: String): List<String> = input.lines()

  override fun format(output: Int): String = output.toString()

  override fun part1(input: List<String>): Int =
    input.sumOf {
      val l = it.length / 2
      val x = it.take(l).toSet()
      val y = it.drop(l).toSet()

      priority((x intersect y).single())
    }

  override fun part2(input: List<String>): Int =
    input.chunked(3).sumOf { (x, y, z) ->
      priority((x.toSet() intersect y.toSet() intersect z.toSet()).single())
    }

  private fun priority(item: Char) =
    if (item.isLowerCase())
      item.code - 'a'.code + 1
    else
      item.code - 'A'.code + 27
}

fun main() = solution.run()
