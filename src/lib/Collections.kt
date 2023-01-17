package lib

import java.util.function.Predicate

typealias Point = Pair<Int, Int>
typealias Grid<T> = List<List<T>>

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

  operator fun Point.plus(other: Point): Point = first + other.first to second + other.second

  operator fun Point.minus(other: Point): Point = first - other.first to second - other.second

  operator fun Point.times(scale: Int): Point = first * scale to second * scale

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

  fun <T, R> Grid<T>.mapIndexed2(transform: (Point, T) -> R): Grid<R> =
    mapIndexed { r, row ->
      row.mapIndexed { c, col ->
        transform(r to c, col)
      }
    }

  fun <T, R> Grid<T>.map2(transform: (T) -> R): Grid<R> =
    mapIndexed2 { _, value -> transform(value) }

  fun <T> Grid<T>.forEachIndexed2(transform: (Point, T) -> Unit) {
    mapIndexed2(transform)
  }

  fun <T> Grid<T>.forEach2(transform: (T) -> Unit) {
    map2(transform)
  }

  fun <T> Grid<T>.zip2(other: List<List<T>>, transform: (T, T) -> T): Grid<T> =
    zip(other) { x, y -> x.zip(y, transform) }

  operator fun <T> Grid<T>.get(point: Point): T = this[point.first][point.second]

  operator fun <T> Grid<T>.contains(point: Point): Boolean =
    (point.first in indices) && (point.second in first().indices)

  fun <T> Grid<T>.neighbours(
    point: Point,
    diagonal: Boolean = false,
    self: Boolean = false,
  ): List<Point> {
    val diff = buildList {
      addAll(listOf((-1 to 0), (1 to 0), (0 to -1), (0 to 1)))
      if (diagonal) {
        addAll(listOf((-1 to -1), (-1 to 1), (1 to -1), (1 to 1)))
      }
      if (self) {
        add(0 to 0)
      }

    }
    return diff.map { d -> point + d }.filter { it in this }
  }

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
