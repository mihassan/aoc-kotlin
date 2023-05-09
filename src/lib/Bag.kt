package lib

import lib.Collections.histogram

data class Bag<T> private constructor(
  val entries: Map<T, Int> = mapOf<T, Int>().withDefault { 0 },
) {
  init {
    require(entries.all { (_, c) -> c > 0 })
  }

  val size: Int get() = entries.values.sum()

  operator fun get(key: T): Int = entries.getValue(key)

  operator fun contains(key: T): Boolean = this[key] > 0

  infix fun isSubSetOf(other: Bag<T>): Boolean = entries.all { (t, c) -> c <= other[t] }

  operator fun plus(key: T): Bag<T> {
    val newEntries = entries.toMutableMap().withDefault { 0 }
    newEntries[key] = newEntries.getValue(key) + 1
    return Bag(newEntries)
  }

  operator fun plus(other: Bag<T>): Bag<T> {
    val newEntries = entries.toMutableMap().withDefault { 0 }
    other.entries.forEach { (t, c) ->
      newEntries[t] = newEntries.getValue(t) + c
    }
    return Bag(newEntries)
  }

  operator fun minus(key: T): Bag<T> {
    val newEntries = entries.toMutableMap().withDefault { 0 }
    newEntries[key] = newEntries.getValue(key) - 1
    if (newEntries.getValue(key) <= 0) {
      newEntries.remove(key)
    }
    return Bag(newEntries)
  }

  operator fun minus(other: Bag<T>): Bag<T> {
    val newEntries = entries.toMutableMap().withDefault { 0 }
    other.entries.forEach { (t, c) ->
      newEntries[t] = newEntries.getValue(t) - c
      if (newEntries.getValue(t) <= 0) {
        newEntries.remove(t)
      }
    }
    return Bag(newEntries)
  }

  operator fun times(multiplicand: Int): Bag<T> =
    Bag(entries.mapValues { (_, c) -> c * multiplicand })

  companion object {
    fun <T> of(entries: Map<T, Int>): Bag<T> = Bag(entries.withDefault { 0 })
    fun <T> of(items: List<T>): Bag<T> = of(items.histogram())
    fun <T> of(vararg items: T): Bag<T> = of(items.toList())
  }
}
