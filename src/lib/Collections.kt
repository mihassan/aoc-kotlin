package lib

object Collections {
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
}
