fun main() {
  fun part1(input: List<Pair<String, Int>>): Int {
    val m = input.groupBy({ it.first }, { it.second }).mapValues { it.value.sum() }
    return m.getOrZero("forward") * (m.getOrZero("down") - m.getOrZero("up"))
  }

  fun part2(input: List<Pair<String, Int>>): Int {
    var (aim, x, y) = arrayOf(0, 0, 0)
    input.forEach { (cmd, X) ->
      when (cmd) {
        "down" -> aim += X
        "up" -> aim -= X
        "forward" -> {
          x += X
          y += aim * X
        }
      }
    }
    return x * y
  }

  val input = readInput("Day02")
    .map { it.words() }
    .map { it[0] to it[1].toInt() }

  println(part1(input))
  println(part2(input))
}
