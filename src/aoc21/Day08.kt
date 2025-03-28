@file:Suppress("PackageDirectoryMismatch")

package aoc21.day08

import lib.Solution

/** A digit is a set of segments, where each segment is represented by a character. */
private data class Digit(val segments: Set<Char>) {
  /** The number of segments in the digit. */
  val segmentCount get() = segments.size

  /** Whether this digit covers the other digit, e.g., 9 covers 4. */
  infix fun covers(other: Digit): Boolean = segments.containsAll(other.segments)
}

private class DigitClassifier(private val knownDigits: MutableMap<Int, Digit> = mutableMapOf()) {
  /**
   * The classification round is the first step in classifying the digits.
   * This is needed as some digits need information from other digits to be classified.
   * For example, to differentiate between 2 and 5, we need to know if 6 covers the digit.
   */
  fun classificationRound(digit: Digit): Int =
    when (digit.segmentCount) {
      2, 3, 4, 7 -> 1
      6 -> 2
      5 -> 3
      else -> error("Unexpected digit: $digit")
    }

  /**
   * Classifies the digit based on the known digits.
   */
  fun classify(digit: Digit): Int {
    if (digit in knownDigits.values) {
      return knownDigits.entries.first { it.value == digit }.key
    }
    val digitId = when (digit.segmentCount) {
      2 -> 1
      3 -> 7
      4 -> 4
      7 -> 8
      6 -> when {
        digit covers 4 -> 9
        digit covers 1 -> 0
        else -> 6
      }

      5 -> when {
        digit covers 1 -> 3
        6 covers digit -> 5
        else -> 2
      }

      else -> error("Unexpected digit: $digit")
    }

    knownDigits[digitId] = digit

    return digitId
  }

  private infix fun Digit.covers(other: Int): Boolean =
    this covers (knownDigits[other] ?: error("Unknown digit: $other while classifying $this"))

  private infix fun Int.covers(other: Digit): Boolean =
    (knownDigits[this] ?: error("Unknown digit: $this while classifying $other")) covers other
}

private data class Entry(val patterns: List<Digit>, val output: List<Digit>) {
  companion object {
    fun parse(entryStr: String): Entry {
      val (patternsStr, outputStr) = entryStr.split(" | ")
      val patterns = patternsStr.split(" ").map { Digit(it.toSet()) }
      val output = outputStr.split(" ").map { Digit(it.toSet()) }
      return Entry(patterns, output)
    }
  }
}

private typealias Input = List<Entry>

private typealias Output = Int

private val solution = object : Solution<Input, Output>(2021, "Day08") {
  override fun parse(input: String): Input = input.lines().map(Entry::parse)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.sumOf { entry ->
      entry.output.count { output ->
        output.segmentCount in setOf(2, 3, 4, 7)
      }
    }

  override fun part2(input: Input): Output =
    input.sumOf { entry ->
      val digits: Map<Digit, Int> = entry.patterns.classifyDigits()
      entry.output
        .map { digits[it] }
        .joinToString("") { it.toString() }
        .toInt()
    }

  fun List<Digit>.classifyDigits(): Map<Digit, Int> {
    require(this.size == 10) { "Expected 10 digits, got ${this.size}" }

    val digitClassifier = DigitClassifier()

    return sortedBy { digitClassifier.classificationRound(it) }
      .associate { it to digitClassifier.classify(it) }
  }
}

fun main() = solution.run()
