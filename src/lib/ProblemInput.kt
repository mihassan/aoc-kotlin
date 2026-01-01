package lib

import lib.Strings.ints
import lib.Strings.longs

/**
 * A wrapper around raw problem input that provides convenient parsing helpers.
 *
 * This class reduces boilerplate in solution files by providing common parsing patterns
 * used across Advent of Code puzzles.
 */
@JvmInline
value class ProblemInput(val raw: String) {

  // Basic accessors
  override fun toString(): String = raw

  fun lines(): List<String> = raw.lines()

  fun chars(): List<Char> = raw.toList()

  // Generic transform helpers
  fun <T> linesAs(transform: (String) -> T): List<T> = lines().map(transform)

  fun <T> charsAs(transform: (Char) -> T): List<T> = chars().map(transform)

  fun split(delimiter: String): List<String> = raw.split(delimiter)

  fun <T> splitAs(delimiter: String, transform: (String) -> T): List<T> =
    split(delimiter).map { transform(it.trim()) }

  fun <T> commaSplitAs(transform: (String) -> T): List<T> = splitAs(",", transform)

  // Number parsing - auto-detects separator (comma, newline, or whitespace)
  fun ints(): List<Int> = when {
    "," in raw -> raw.split(",").map { it.trim().toInt() }
    "\n" in raw -> lines().map { it.trim().toInt() }
    else -> raw.ints()
  }

  fun longs(): List<Long> = when {
    "," in raw -> raw.split(",").map { it.trim().toLong() }
    "\n" in raw -> lines().map { it.trim().toLong() }
    else -> raw.longs()
  }

  // Grid parsing
  fun charGrid(): Grid<Char> = Grid.parse(raw)

  fun <T> gridAs(transform: (Char) -> T): Grid<T> = charGrid().map(transform)

  fun digitGrid(): Grid<Int> = gridAs { it.digitToInt() }

  // Section parsing - splits on blank lines
  fun sections(): List<ProblemInput> = raw.split("\n\n").map { ProblemInput(it) }

  fun <T> sectionsAs(transform: (ProblemInput) -> T): List<T> = sections().map(transform)
}

