package lib

object Strings {
  private val SPACE_REGEX = "\\s+".toRegex()

  fun String.words() = trim().split(SPACE_REGEX)
  fun String.ints() = words().map { it.toInt() }
  fun String.longs() = words().map { it.toLong() }
  fun String.doubles() = words().map { it.toDouble() }

  fun String.isInt() = toIntOrNull() != null
  fun String.isLong() = toLongOrNull() != null
  fun String.isDouble() = toDoubleOrNull() != null

  fun String.extractInts() = Regex("[-+]?\\d+").findAll(this).map { it.value.toInt() }.toList()
  fun String.extractLongs() = Regex("[-+]?\\d+").findAll(this).map { it.value.toLong() }.toList()

  fun String.splitIn(parts: Int): List<String> = chunked(length / parts)

  infix fun String.intersect(other: String): Set<Char> = toSet() intersect other.toSet()
  infix fun String.intersect(other: Set<Char>): Set<Char> = toSet() intersect other
  infix fun Set<Char>.intersect(other: String): Set<Char> = this intersect other.toSet()
}
