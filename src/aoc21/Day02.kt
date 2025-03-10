@file:Suppress("PackageDirectoryMismatch")

package aoc21.day02

import lib.Solution
import lib.Strings.words

private val solution = object : Solution<List<Pair<String, Int>>, Int>(2021, "Day02") {
  override fun parse(input: String): List<Pair<String, Int>> =
    input.lines().map { it.words() }.map { (d, s) -> d to s.toInt() }

  override fun format(output: Int): String = output.toString()

  override fun part1(input: List<Pair<String, Int>>): Int {
    val m = input.groupBy({ it.first }, { it.second }).mapValues { it.value.sum() }
    return (m["forward"] ?: 0) * ((m["down"] ?: 0) - (m["up"] ?: 0))
  }

  override fun part2(input: List<Pair<String, Int>>): Int {
    var (aim, x, y) = arrayOf(0, 0, 0)
    input.forEach { (cmd, dx) ->
      when (cmd) {
        "down" -> aim += dx
        "up" -> aim -= dx
        "forward" -> {
          x += dx
          y += aim * dx
        }
      }
    }
    return x * y
  }
}

fun main() = solution.run()
