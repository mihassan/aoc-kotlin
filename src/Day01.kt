import lib.Solution
import lib.Strings.ints

private val solution = object : Solution<List<Int>, Int>("Day01") {
  override fun parse(input: String): List<Int> = input.ints()

  override fun format(output: Int): String = output.toString()

  override fun part1(input: List<Int>): Int = input
    .zipWithNext()
    .count { (a, b) -> b > a }

  override fun part2(input: List<Int>): Int = input
    .windowed(3)
    .map(List<Int>::sum)
    .let(::part1)
}

fun main() = solution.run()
