fun main() {
  fun part1(input: List<Int>) = input.zipWithNext().count { (a, b) -> b > a }

  fun part2(input: List<Int>) = input.windowed(3).map(List<Int>::sum).let(::part1)

  val input = readInts("Day01")
  println(part1(input))
  println(part2(input))
}
