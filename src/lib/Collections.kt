package lib

import java.util.function.Predicate

typealias Grid<T> = List<List<T>>

object Collections {
  fun <T> List<T>.prefixes() = (1..size).map { take(it) }
  fun <T> List<T>.suffixes() = indices.map { drop(it) }

  fun <T> List<T>.splitIn(parts: Int): List<List<T>> = chunked(size / parts)

  fun List<Int>.cumulativeSum1() = runningFold(0) { x, y -> x + y }
  fun List<Int>.cumulativeSum() = cumulativeSum1().drop(1)

  @JvmName("cumulativeSum1Long")
  fun List<Long>.cumulativeSum1() = runningFold(0L) { x, y -> x + y }

  @JvmName("cumulativeSumLong")
  fun List<Long>.cumulativeSum() = cumulativeSum1().drop(1)

  fun <E> List<E>.isDistinct() = size == toSet().size

  fun <T> Grid<T>.transposed(): Grid<T> {
    val rows = size
    val cols = map { it.size }.toSet().single()

    return List(cols) { col ->
      List(rows) { row ->
        this[row][col]
      }
    }
  }

  fun <T> Grid<T>.flipVertically(): Grid<T> = reversed()

  fun <T> Grid<T>.flipHorizontally(): Grid<T> = map(List<T>::reversed)

  fun <T> Grid<T>.rotateCW(): Grid<T> = flipVertically().transposed()

  fun <T> Grid<T>.rotateCCW(): Grid<T> = flipHorizontally().transposed()

  fun <T> Grid<T>.rotate180(): Grid<T> = flipVertically().flipHorizontally()

  fun <T> Grid<T>.zip2(other: List<List<T>>, transform: (T, T) -> T): Grid<T> =
    zip(other) { x, y -> x.zip(y, transform) }

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

  fun <T> List<T>.partitions(predicate: Predicate<T>): List<List<T>> {
    val groups = mutableListOf<MutableList<T>>()
    forEach { e ->
      if (predicate.test(e)) {
        groups.add(mutableListOf(e))
      } else {
        groups.last().add(e)
      }
    }
    return groups
  }
}
