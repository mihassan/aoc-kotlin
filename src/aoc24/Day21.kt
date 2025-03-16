@file:Suppress("PackageDirectoryMismatch")

package aoc24.day21

import aoc24.day21.Keypad.DirectionKeypad
import aoc24.day21.Keypad.DirectionKeypad.computeMovesToApply
import aoc24.day21.Keypad.NumericKeypad
import kotlin.math.absoluteValue
import lib.Collections.histogram
import lib.Point
import lib.Solution

typealias Input = List<String>

typealias Output = Long

typealias Move = Char
typealias Moves = String

sealed class Keypad() {
  abstract val layout: Map<Move, Point>
  abstract val hole: Point

  /**
   * Return the sequence of moves to move from the source key to the destination key.
   * Regardless of the Keypad on which the move is performed,
   * the output move sequence can be entered into a DIRECTION keypad.
   *
   * The algorithm is greedy and tries to avoid the hole as much as possible.
   * The key observation is that when moving right, prefer to move in y-axis first.
   * Otherwise, move in x-axis first.
   * The idea is borrowed from the following Reddit post by u/Prof_McBurney:
   * https://www.reddit.com/r/adventofcode/comments/1hjgyps/2024_day_21_part_2_i_got_greedyish.
   *
   * @param src The source key.
   * @param dst The destination key.
   * @return The sequence of direction moves (i.e., '<', '>', '^', 'v') that ends with 'A'.
   */
  fun move(src: Move, dst: Move): String {
    val srcPoint = layout[src] ?: error("Invalid key: $src")
    val dstPoint = layout[dst] ?: error("Invalid key: $dst")
    val (dx, dy) = dstPoint - srcPoint

    val xMoves = List(dx.absoluteValue) { if (dx > 0) '>' else '<' }
    val yMoves = List(dy.absoluteValue) { if (dy > 0) 'v' else '^' }

    val moves = when {
      // Avoid the hole when moving from the row of the hole to the column of the hole
      // by moving column first and then row.
      Point(dstPoint.x, srcPoint.y) == hole -> yMoves + xMoves
      // Avoid the hole when moving from the column of the hole to the row of the hole
      // by moving row first and then column.
      Point(srcPoint.x, dstPoint.y) == hole -> xMoves + yMoves
      // When going right, prefer to move in y-axis first.
      // This is the key observation to keep the algorithm greedy.
      // The idea is borrowed from the following Reddit post by u/Prof_McBurney:
      // https://www.reddit.com/r/adventofcode/comments/1hjgyps/2024_day_21_part_2_i_got_greedyish
      dx > 0 -> yMoves + xMoves
      // Otherwise move in x-axis first.
      else -> xMoves + yMoves
    }

    return moves.joinToString("") + 'A'
  }

  /**
   * Compute the moves that needs to be applied to a DIRECTION keypad so that the moves are applied.
   * It assumes that the robot hand is currently at the 'A' key.
   */
  open fun computeMovesToApply(moves: Moves): Moves =
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

    /**
     * Override the [computeMovesToApply] method to memoize the result.
     * This is because the computation is expensive and the result is deterministic,
     * provided that the robot hand starts at the 'A' key and ends at the 'A' key.
     */
    override fun computeMovesToApply(moves: Moves): Moves =
      cache.getOrPut(moves) { super.computeMovesToApply(moves) }

    private val cache: MutableMap<Moves, Moves> = mutableMapOf()
  }
}

/** A segment is a sequence of moves that ends with 'A'. */
@ConsistentCopyVisibility
data class Segment private constructor(val moves: Moves) {
  val length: Int get() = moves.length

  companion object {
    private val SEGMENT_PATTERN = """[^A]*A""".toRegex()

    fun fromMoves(moves: Moves): List<Segment> =
      SEGMENT_PATTERN.findAll(moves).map { Segment(it.value) }.toList()
  }
}

/**
 * A compressed move is a [Map] from a segment to the number of times it is repeated.
 * We can compress like this because the order of segments does not matter to the final count.
 */
@ConsistentCopyVisibility
data class CompressedMoves private constructor(private val segments: Map<Segment, Long>) {
  val length: Long get() = segments.entries.sumOf { (segment, count) -> segment.length * count }

  /**
   * Find the moves needed to be applied to the keypad to produce the given compressed moves.
   * */
  fun computeMovesToApply(): CompressedMoves =
    segments.flatMap { (segment, count) ->
      DirectionKeypad
        .computeMovesToApply(segment.moves)
        .let(Segment::fromMoves)
        .map { it to count }
    }.let(::fromSegmentCountPairs)

  companion object {
    /** Create a [CompressedMoves] from a list of moves by splitting them into segments first. */
    fun fromMoves(moves: Moves): CompressedMoves =
      CompressedMoves(Segment.fromMoves(moves).histogram().mapValues { it.value.toLong() })

    /**
     * Create a [CompressedMoves] from a list of segment-count pairs.
     * There can be multiple segments with the same moves.
     * So, we need to group them by the moves and sum the counts.
     */
    fun fromSegmentCountPairs(segments: List<Pair<Segment, Long>>): CompressedMoves =
      segments
        .groupingBy { it.first }.fold(0L) { acc, (_, count) -> acc + count }
        .let(::CompressedMoves)
  }
}


private val solution = object : Solution<Input, Output>(2024, "Day21") {
  override fun parse(input: String): Input = input.lines()

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.sumOf {
      computeComplexityOfTheChain(it, 3)
    }

  override fun part2(input: Input): Output =
    input.sumOf {
      computeComplexityOfTheChain(it, 26)
    }

  /**
   * Compute the complexity of the initial moves to be applied to the DIRECTION keypad.
   * The moves are applied to a sequence of [chainLength] DIRECTION and NUMERIC keypads.
   * All but the last keypad are DIRECTION keypads, only the last keypad is a NUMERIC keypad.
   */
  private fun computeComplexityOfTheChain(numericMoves: Moves, chainLength: Int): Long {
    var moves: CompressedMoves =
      CompressedMoves.fromMoves(NumericKeypad.computeMovesToApply(numericMoves))
    repeat(chainLength - 1) {
      moves = moves.computeMovesToApply()
    }
    return moves.length * numericMoves.numericValue()
  }

  private fun String.numericValue(): Long =
    this.dropWhile { it == '0' }.dropLastWhile { it == 'A' }.toLong()
}

fun main() =  solution.run()
