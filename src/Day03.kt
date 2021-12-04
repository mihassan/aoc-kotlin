enum class Day03Rating {
  O2,
  CO2
}

fun main() {
  fun countBits(input: List<String>) =
    input.flatMap { it.mapIndexed(::Pair) }
      .groupBy({ it.first }, { it.second })
      .mapValues { it.value.freq() }

  fun gamma(input: Map<Int, Map<Char, Int>>) = input.mapValues {
    it.value.maxByOrNull { it.value }?.key ?: '0'
  }

  fun epsilon(input: Map<Int, Map<Char, Int>>) = input.mapValues {
    it.value.minByOrNull { it.value }?.key ?: '0'
  }

  fun Map<Int, Char>.readBits() = values.toCharArray().concatToString().toInt(2)

  fun part1(input: List<String>) = countBits(input).let {
    gamma(it).readBits() * epsilon(it).readBits()
  }

  fun bitCriteria(bitCount: Map<Char, Int>, rating: Day03Rating): Char {
    return bitCount['1']?.let { one ->
      bitCount['0']?.let { zero ->
        when (rating) {
          Day03Rating.O2 -> if (one >= zero) '1' else '0'
          Day03Rating.CO2 -> if (one < zero) '1' else '0'
        }
      } ?: '1'
    } ?: '0'
  }

  tailrec fun filterValues(values: List<String>, rating: Day03Rating, bit: Int = 0): String {
    if (values.size == 1) {
      return values[0]
    }
    val bitCount = values.map { it[bit] }.freq()
    val selectedBit = bitCriteria(bitCount, rating)
    return filterValues(values.filter { it[bit] == selectedBit }, rating, bit + 1)
  }

  fun part2(input: List<String>) =
    filterValues(input, Day03Rating.O2).toInt(2) *
      filterValues(input, Day03Rating.CO2).toInt(2)

  val input = readInput("Day03")
  println(part1(input))
  println(part2(input))
}
