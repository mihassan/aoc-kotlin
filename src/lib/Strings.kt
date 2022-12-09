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
}
