@file:Suppress("PackageDirectoryMismatch")

package aoc22.day09

import kotlin.math.abs
import kotlin.math.sign
import lib.Collections.headTail
import lib.Solution

data class Point(val x: Int, val y: Int) {
  val absoluteValue by lazy { Point(abs(x), abs(y)) }
  val signValue by lazy { Point(sign(x.toDouble()).toInt(), sign(y.toDouble()).toInt()) }
}

operator fun Point.plus(o: Point): Point = Point(x + o.x, y + o.y)

operator fun Point.minus(o: Point): Point = Point(x - o.x, y - o.y)

operator fun Point.times(s: Int): Point = Point(x * s, y * s)

infix fun Point.touches(o: Point): Boolean =
  (this - o).absoluteValue.let { (x, y) -> x <= 1 && y <= 1 }

enum class Direction(val delta: Point) {
  R(Point(1, 0)),
  L(Point(-1, 0)),
  U(Point(0, 1)),
  D(Point(0, -1))
}

fun String.toDirection(): Direction = when (this) {
  "R" -> Direction.R
  "L" -> Direction.L
  "U" -> Direction.U
  "D" -> Direction.D
  else -> error("Invalid direction.")
}

data class Step(val direction: Direction, val stepCount: Int) {
  val breakdown: List<Direction> = List(stepCount) { direction }
}

fun String.toStep(): Step = split(" ").let { (d, s) -> Step(d.toDirection(), s.toInt()) }

typealias Input = List<Direction>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2022, "Day09") {
  val KNOT_COUNT = mapOf(Part.PART1 to 2, Part.PART2 to 10)

  override fun parse(input: String): Input = input.lines().flatMap { it.toStep().breakdown }

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output {
    val knotCount = checkNotNull(KNOT_COUNT[part])
    val initialKnots = List(knotCount) { Point(0, 0) }
    val ropeSequence = input.runningFold(initialKnots, ::moveRope)
    return ropeSequence.map { it.last() }.toSet().size
  }

  private fun moveRope(knots: List<Point>, direction: Direction): List<Point> {
    val (head, tail) = knots.headTail()
    val newHead = checkNotNull(head) + direction.delta
    return tail.runningFold(newHead) { acc, knot ->
      followNextKnot(knot, acc)
    }
  }

  private fun followNextKnot(followedKnot: Point, followingKnot: Point): Point =
    if (followingKnot touches followedKnot)
      followedKnot
    else
      followedKnot + (followingKnot - followedKnot).signValue
}

fun main() = solution.run()
