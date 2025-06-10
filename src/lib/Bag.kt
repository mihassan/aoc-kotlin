package lib

import lib.Collections.histogram

@JvmInline
@Suppress("NOTHING_TO_INLINE")
value class Bag<T>(val entries: MutableMap<T, Int> = mutableMapOf()) {
  init {
    entries.forEach { (t, c) ->
      if (c <= 0) {
        entries.remove(t)
      }
    }
  }

  inline val size: Int get() = entries.values.sum()

  operator fun get(key: T): Int = entries[key] ?: 0

  operator fun set(key: T, count: Int) {
    entries[key] = count
  }

  inline operator fun contains(key: T): Boolean = this[key] > 0

  inline infix fun isSubSetOf(other: Bag<T>): Boolean =
    entries.all { (t, c) -> c <= other[t] }

  inline operator fun plus(key: T): Bag<T> {
    val newBag = Bag<T>()
    newBag += this
    newBag += key
    return newBag
  }

  inline operator fun plus(other: Bag<T>): Bag<T> {
    val newBag = Bag<T>()
    newBag += this
    newBag += other
    return newBag
  }

  inline operator fun minus(other: Bag<T>): Bag<T> {
    val newBag = Bag<T>()
    newBag += this
    newBag -= other
    return newBag
  }

  inline operator fun minus(key: T): Bag<T> {
    val newBag = Bag<T>()
    newBag += this
    newBag -= key
    return newBag
  }

  operator fun times(multiplicand: Int): Bag<T> {
    val newBag = Bag(entries.toMutableMap())
    newBag *= multiplicand
    return newBag
  }

  inline operator fun plusAssign(key: T) {
    this[key] += 1
  }

  inline operator fun plusAssign(other: Bag<T>) {
    other.entries.forEach { (t, c) ->
      require(c > 0)
      this[t] += c
    }
  }

  inline operator fun minusAssign(key: T) {
    val newValue = this[key] - 1
    if (newValue > 0) {
      this[key] = newValue
    } else {
      entries.remove(key)
    }
  }

  inline operator fun minusAssign(other: Bag<T>) {
    other.entries.forEach { (t, c) ->
      val newValue = this[t] - c
      if (newValue > 0) {
        this[t] = newValue
      } else {
        entries.remove(t)
      }
    }
  }

  operator fun timesAssign(multiplicand: Int) {
    entries.replaceAll { _, c -> c * multiplicand }
  }

  fun forEach(action: (T, Int) -> Unit) {
    entries.forEach { (t, c) -> action(t, c) }
  }

  companion object {
    fun <T> of(entries: Map<T, Int>): Bag<T> = Bag(entries.toMutableMap())
    fun <T> of(items: List<T>): Bag<T> = of(items.histogram())
    fun <T> of(vararg pairs: Pair<T, Int>): Bag<T> = Bag(mutableMapOf(*pairs))
  }
}
