@file:Suppress("PackageDirectoryMismatch")

package aoc24.day21

import aoc24.day21.Keypad.DirectionKeypad
import aoc24.day21.Keypad.NumericKeypad
import kotlin.math.absoluteValue
import lib.Collections.histogram
import lib.DP.memoize
import lib.Point
import lib.Solution

typealias Input = List<String>

typealias Output = Long

typealias Move = Char
typealias Moves = String

sealed class Keypad() {
  abstract val layout: Map<Move, Point>
  abstract val hole: Point

  fun move(src: Move, dst: Move): String {
    val srcPoint = layout[src] ?: error("Invalid key: $src")
    val dstPoint = layout[dst] ?: error("Invalid key: $dst")
    val (dx, dy) = dstPoint - srcPoint

    val xMoves = List(dx.absoluteValue) { if (dx > 0) '>' else '<' }
    val yMoves = List(dy.absoluteValue) { if (dy > 0) 'v' else '^' }

    val moves = when {
      // Avoid the hole when moving from the row of the hole to the column of the hole
      srcPoint.y == hole.y && dstPoint.x == hole.x -> yMoves + xMoves
      // Avoid the hole when moving from the column of the hole to the row of the hole
      srcPoint.x == hole.x && dstPoint.y == hole.y -> xMoves + yMoves
      // When going right, prefer to move in y-axis first
      dx > 0 -> yMoves + xMoves
      // Otherwise move in x-axis first
      else -> xMoves + yMoves
    }

    return moves.joinToString("") + 'A'
  }

  fun applyMoves(moves: Moves): Moves =
    "A$moves".zipWithNext { a, b -> move(a, b) }.joinToString("")

  object NumericKeypad : Keypad() {
    override val hole: Point = Point(0, 3)
    override val layout = mapOf(
      '7' to Point(0, 0),
      '8' to Point(1, 0),
      '9' to Point(2, 0),
      '4' to Point(0, 1),
      '5' to Point(1, 1),
      '6' to Point(2, 1),
      '1' to Point(0, 2),
      '2' to Point(1, 2),
      '3' to Point(2, 2),
      '0' to Point(1, 3),
      'A' to Point(2, 3)
    )
  }

  object DirectionKeypad : Keypad() {
    override val hole: Point = Point(0, 0)
    override val layout = mapOf(
      '^' to Point(1, 0),
      'A' to Point(2, 0),
      '<' to Point(0, 1),
      'v' to Point(1, 1),
      '>' to Point(2, 1)
    )
  }
}

// A segment is a sequence of moves that ends with 'A'.
typealias Segment = String

// A compressed move is a map from a segment to the number of times it is repeated.
// We can compress like this because the order of segments does not matter.
typealias CompressedMoves = Map<Segment, Long>

/**
 * Apply the given segment of moves to the keypad and return the compressed moves.
 */
private fun applyMoveSegment(keypad: Keypad, segment: Segment): CompressedMoves =
  keypad.applyMoves(segment).splitIntoSegments().histogram().mapValues { it.value.toLong() }

/**
 * Memoized version of [applyMoveSegment].
 */
private val applyMoveSegmentMemo: (Keypad, Segment) -> CompressedMoves =
  memoize { keypad, segment -> applyMoveSegment(keypad, segment) }

private fun String.splitIntoSegments(): List<Segment> =
  Regex("""[^A]*A""").findAll(this).map { it.value }.toList()

private val solution = object : Solution<Input, Output>(2024, "Day21") {
  override fun parse(input: String): Input = input.lines()

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.sumOf {
      applyMovesThroughChain(it, 3)
    }

  override fun part2(input: Input): Output =
    input.sumOf {
      applyMovesThroughChain(it, 26)
    }

  private fun applyMovesThroughChain(initialMoves: Moves, step: Int): Long {
    var finalMoves: CompressedMoves = applyMoveSegmentMemo(NumericKeypad, initialMoves)
    repeat(step - 1) {
      finalMoves = finalMoves.applyMoves()
    }
    return finalMoves.length() * initialMoves.numericValue()
  }

  private fun CompressedMoves.applyMoves(): CompressedMoves =
    flatMap { (segment, count) ->
      applyMoveSegmentMemo(
        DirectionKeypad,
        segment
      ).mapValues { (_, count2) -> count * count2 }.entries
    }.groupBy({ it.key }, { it.value }).mapValues { (_, counts) -> counts.sum() }

  private fun CompressedMoves.length(): Long =
    this.map { (segment, count) -> segment.length.toLong() * count }.sum().toLong()

  private fun String.numericValue(): Long =
    this.dropWhile { it == '0' }.dropLastWhile { it == 'A' }.toLong()
}

fun main() {
  solution.run()
}
