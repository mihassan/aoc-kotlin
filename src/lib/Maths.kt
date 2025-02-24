package lib

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

object Maths {
  fun Int.isOdd() = this % 2 == 1
  fun Long.isOdd() = this % 2 == 1L
  fun Int.isEven() = !isOdd()
  fun Long.isEven() = !isOdd()
  fun Int.isZero() = this == 0
  fun Long.isZero() = this == 0L
  fun Int.isPositive() = this > 0
  fun Long.isPositive() = this > 0L
  fun Int.isNegative() = this < 0
  fun Long.isNegative() = this < 0L
  fun Int.isPositiveOrZero() = this >= 0
  fun Long.isPositiveOrZero() = this >= 0L
  fun Int.isNegativeOrZero() = this <= 0
  fun Long.isNegativeOrZero() = this <= 0L

  infix fun Int.divides(o: Int) = o % this == 0
  infix fun Long.divides(o: Long) = o % this == 0L
  infix fun Int.divisibleBy(o: Int) = o divides this
  infix fun Long.divisibleBy(o: Long) = o divides this

  infix fun Int.floorDiv(o: Int) = floor(toDouble() / o).toInt()
  infix fun Long.floorDiv(o: Long) = floor(toDouble() / o).toInt()
  infix fun Int.ceilDiv(o: Int) = ceil(toDouble() / o).toInt()
  infix fun Long.ceilDiv(o: Long) = ceil(toDouble() / o).toInt()

  infix fun Int.gcd(o: Int): Int {
    if (o == 0) return this
    return o gcd (this % o)
  }

  infix fun Long.gcd(o: Long): Long {
    if (o == 0L) return this
    return o gcd (this % o)
  }

  infix fun Int.lcm(o: Int): Int {
    return this / (this gcd o) * o
  }

  infix fun Long.lcm(o: Long): Long {
    return this / (this gcd o) * o
  }

  infix fun Int.pow(p: Int): Int = (1..p).fold(1) { acc, _ -> acc * this }
  infix fun Long.pow(p: Long): Long = (1..p).fold(1) { acc, _ -> acc * this }

  infix fun Int.mod(m: Int) = ((this % m) + m) % m
  infix fun Long.mod(m: Long) = ((this % m) + m) % m

  fun primeSieve(n: Int): List<Boolean> {
    val s = BooleanArray(n + 1) { true }
    val limit = sqrt(n.toDouble()).toInt()
    (2..limit).forEach { i ->
      if (s[i]) (i * i..n).forEach { j -> s[j] = false }
    }
    return s.toList()
  }

  fun primes(n: Int) = primeSieve(n).mapIndexedNotNull { i, p -> i.takeIf { p } }
}