package lib

import kotlin.math.min
import kotlin.math.max

object Ranges {
  val IntRange.size
    get() = when {
      isEmpty() -> 0
      else -> last - first + 1
    }

  operator fun IntRange.contains(other: IntRange): Boolean =
    other.first in this && other.last in this

  infix fun IntRange.overlaps(other: IntRange): Boolean {
    check(step > 0 && other.step > 0)
    return first <= other.last && last >= other.first
  }

  infix fun IntRange.union(other: IntRange): IntRange {
    check(step > 0 && other.step > 0)
    check(this overlaps other)
    return min(first, other.first)..max(last, other.last)
  }

  infix fun IntRange.intersect(other: IntRange): IntRange {
    check(step > 0 && other.step > 0)
    return when {
      this overlaps other -> max(first, other.first)..min(last, other.last)
      else -> IntRange.EMPTY
    }
  }
}
