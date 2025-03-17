@file:Suppress("PackageDirectoryMismatch")

package aoc24.day22

import lib.Solution
import lib.Strings.longs

typealias Input = List<Long>

typealias Output = Long

fun Long.next() =
  stage1()
    .stage2()
    .stage3()

fun Long.stage1() = mix(this * 64).prune()

fun Long.stage2() = mix(this / 32).prune()

fun Long.stage3() = mix(this * 2048).prune()

fun Long.mix(other: Long) = this xor other

fun Long.prune() = this and 0xFFFFFF

fun Long.secrets(steps: Int): List<Long> = buildList {
  var secret = this@secrets
  add(secret)
  repeat(steps) {
    secret = secret.next()
    add(secret)
  }
}

fun Long.prices(steps: Int): List<Long> =
  secrets(steps).map { it % 10 }

fun Long.priceChanges(steps: Int): List<Long> =
  prices(steps).zipWithNext { a, b -> b - a }

fun Long.pricesWithLastNChanges(steps: Int, n: Int): List<Pair<Long, List<Long>>> {
  // drop the first n prices as they don't have n changes before them.
  val prices = prices(steps).drop(n)
  val lastNChanges = priceChanges(steps).windowed(n)
  return prices.zip(lastNChanges)
}

private val solution = object : Solution<Input, Output>(2024, "Day22") {
  override fun parse(input: String): Input = input.longs()

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.sumOf { it.secrets(2000).last() }

  override fun part2(input: Input): Output {
    val changes: MutableMap<List<Long>, Long> = mutableMapOf()

    input.forEach { secret ->
      val seen = mutableSetOf<List<Long>>()
      secret.pricesWithLastNChanges(2000, 4).forEach { (price, last4Changes) ->
        if (last4Changes !in seen) {
          seen += last4Changes
          if (last4Changes !in changes) {
            changes[last4Changes] = price
          } else {
            changes[last4Changes] = changes[last4Changes]!! + price
          }
        }
      }
    }

    return changes.values.max()
  }
}

fun main() = solution.run()
