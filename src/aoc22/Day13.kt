@file:Suppress("PackageDirectoryMismatch")

package aoc2022.day13

import lib.Collections.headTail
import lib.Solution

sealed interface Value : Comparable<Value> {
  override operator fun compareTo(other: Value): Int =
    when (this) {
      is IntegerValue -> when (other) {
        is IntegerValue -> value.compareTo(other.value)
        is ListValue -> ListValue(listOf(this)).compareTo(other)
      }
      is ListValue -> when (other) {
        is IntegerValue -> compareTo(ListValue(listOf(other)))
        is ListValue -> {
          val (h1, t1) = list.headTail()
          val (h2, t2) = other.list.headTail()
          when {
            h1 == null || h2 == null -> list.size.compareTo(other.list.size)
            h1.compareTo(h2) == 0 -> ListValue(t1).compareTo(ListValue(t2))
            else -> h1.compareTo(h2)
          }
        }
      }
    }

  data class IntegerValue(val value: Int) : Value {
    companion object {
      fun parse(input: ArrayDeque<Char>): IntegerValue {
        assert(input.isNotEmpty() && input.first().isDigit())

        var v = 0
        while (input.first().isDigit()) {
          val d = input.removeFirst().digitToInt()
          v = v * 10 + d
        }

        return IntegerValue(v)
      }
    }
  }

  data class ListValue(val list: List<Value>) : Value {
    companion object {
      fun parse(input: ArrayDeque<Char>): ListValue {
        assert(input.size >= 2 && input.first() == '[')

        // Special case for handling  empty list.
        if (input[1] == ']') {
          repeat(2) {
            input.removeFirst()
          }
          return ListValue(emptyList())
        }

        val l = buildList {
          while (input.removeFirst() != ']') {
            add(Value.parse(input))
          }
        }

        return ListValue(l)
      }
    }
  }

  companion object {
    fun parse(input: String): Value = parse(ArrayDeque(input.toList()))

    fun parse(input: ArrayDeque<Char>): Value =
      if (input.first() == '[') {
        ListValue.parse(input)
      } else {
        IntegerValue.parse(input)
      }
  }
}

typealias Input = List<Pair<Value, Value>>
typealias Output = Int

private val solution = object : Solution<Input, Output>(2022, "Day13") {
  val DIVIDER_PACKETS = listOf(Value.parse("[[2]]"), Value.parse("[[6]]"))

  override fun parse(input: String): Input =
    input
      .split("\n\n")
      .map { block ->
        val (l1, l2) = block.lines()
        Value.parse(l1) to Value.parse(l2)
      }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input
      .mapIndexedNotNull { idx, pair ->
        (idx + 1).takeIf {
          pair.first < pair.second
        }
      }.sum()

  override fun part2(input: Input): Output =
    (input.flatMap { it.toList() } + DIVIDER_PACKETS).sorted()
      .mapIndexedNotNull { idx, packet ->
        (idx + 1).takeIf {
          packet in DIVIDER_PACKETS
        }
      }.reduce(Int::times)
}

fun main() = solution.run()
