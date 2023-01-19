package lib

object Collections {
  fun <T> List<T>.prefixes() = (1..size).map { take(it) }
  fun <T> List<T>.suffixes() = indices.map { drop(it) }

  fun <T> List<T>.splitIn(parts: Int): List<List<T>> = chunked(size / parts)
  fun <T> List<T>.headTail() = firstOrNull() to drop(1)

  fun List<Int>.cumulativeSum1() = runningFold(0) { x, y -> x + y }
  fun List<Int>.cumulativeSum() = cumulativeSum1().drop(1)

  @JvmName("cumulativeSum1Long")
  fun List<Long>.cumulativeSum1() = runningFold(0L) { x, y -> x + y }

  @JvmName("cumulativeSumLong")
  fun List<Long>.cumulativeSum() = cumulativeSum1().drop(1)

  fun <E> List<E>.isDistinct() = size == toSet().size

  fun <T> List<T>.histogram() = groupingBy { it }.eachCount()
  fun String.histogram() = groupingBy { it }.eachCount()

  fun <T> List<T>.groupContiguous(): List<List<T>> {
    val groups = mutableListOf<MutableList<T>>()
    forEach { e ->
      if (e == groups.lastOrNull()?.lastOrNull()) {
        groups.last().add(e)
      } else {
        groups.add(mutableListOf(e))
      }
    }
    return groups
  }

  fun <T> List<T>.partitions(predicate: (T) -> Boolean): List<List<T>> {
    val groups = mutableListOf<MutableList<T>>()
    forEach { e ->
      if (predicate(e)) {
        groups.add(mutableListOf(e))
      } else {
        groups.last().add(e)
      }
    }
    return groups
  }
}
