@file:Suppress("PackageDirectoryMismatch")

package aoc25.day01

import lib.ProblemInput
import lib.Solution
import lib.Strings.splitAt

private enum class Direction(val symbol: Char, val delta: Int) {
  LEFT('L', -1), RIGHT('R', 1);

  companion object {
    fun parse(directionChar: Char): Direction = entries.firstOrNull { it.symbol == directionChar }
      ?: error("No direction with symbol $directionChar")
  }
}

private data class Rotation(val direction: Direction, val steps: Int) {
  companion object {
    fun parse(input: String): Rotation {
      val (directionPart, stepsPart) = input.splitAt(1)
      val direction = directionPart.singleOrNull()?.let(Direction::parse)
        ?: error("Invalid rotation format: $input")
      return Rotation(direction, stepsPart.toInt())
    }
  }
}

/**
 * A dial-based clock that tracks zero-position events.
 *
 * @property size The number of positions on the dial (0 until size).
 * @property position The current dial position.
 * @property zeroLandings Count of rotations that ended at position 0.
 * @property zeroPassings Count of times position 0 was crossed during movement.
 */
private data class Clock(
  val size: Int = DEFAULT_SIZE,
  val position: Int = DEFAULT_SIZE / 2,
  val zeroLandings: Int = 0,
  val zeroPassings: Int = 0,
) {
  /** Applies a single step in the given direction, returning the updated clock. */
  private fun step(direction: Direction): Clock {
    val newPosition = (position + direction.delta).mod(size)
    val crossedZero = newPosition == 0
    return copy(
      position = newPosition,
      zeroPassings = if (crossedZero) zeroPassings + 1 else zeroPassings,
    )
  }

  /** Applies the given rotation, returning the updated clock. */
  fun rotate(rotation: Rotation): Clock {
    val fullLaps = rotation.steps / size
    val remainingSteps = rotation.steps % size

    val afterFullLaps = copy(zeroPassings = zeroPassings + fullLaps)
    val afterRemainingSteps = (1..remainingSteps).fold(afterFullLaps) { clock, _ ->
        clock.step(rotation.direction)
      }

    val zeroLandingsIncrement = if (afterRemainingSteps.position == 0) 1 else 0

    return afterRemainingSteps.copy(zeroLandings = afterRemainingSteps.zeroLandings + zeroLandingsIncrement)
  }

  companion object {
    private const val DEFAULT_SIZE = 100
  }
}

private typealias Input = List<Rotation>

private typealias Output = Int

private val solution = object : Solution<Input, Output>(2025, "Day01") {
  override fun parse(input: ProblemInput): Input = input.linesAs(Rotation::parse)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.fold(Clock()) { clock, rotation -> clock.rotate(rotation) }.zeroLandings

  override fun part2(input: Input): Output =
    input.fold(Clock()) { clock, rotation -> clock.rotate(rotation) }.zeroPassings
}

fun main() = solution.run()
