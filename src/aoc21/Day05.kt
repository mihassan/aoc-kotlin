@file:Suppress("PackageDirectoryMismatch")

package aoc21.day04

import lib.Grid
import lib.Point
import lib.Solution
import lib.Strings.ints

data class Board(val grid: Grid<Int>) {
  /**
   * Returns the 0-based index of the round where the board is winning.
   * Keep track of all unmarked cells. After marking each number check if the board is winning.
   * If there are fewer unmarked rows or columns than the board's dimensions, the board is winning.
   * */
  fun winningRound(numbers: List<Int>): Int {
    val unmarkedCells = grid.indices().toMutableSet()
    numbers.forEachIndexed { idx, number ->
      unmarkedCells.mark(number)
      if (unmarkedCells.isWinning()) {
        return idx
      }
    }
    return 0
  }

  /** Marks the cell containing [number] by removing it from the set of unmarked cells. */
  private fun MutableSet<Point>.mark(number: Int) {
    val unmarkedCells = this
    val cell = grid.indexOfOrNull { it == number } ?: return
    unmarkedCells -= cell
  }

  /**
   * Returns `true` if the board is winning by checking the set of unmarked cells.
   * If there are fewer unmarked rows or columns than the board's dimensions.
   */
  private fun MutableSet<Point>.isWinning(): Boolean {
    val unmarkedCells = this
    val unmarkedRows = unmarkedCells.map { it.y }.toSet()
    val unmarkedCols = unmarkedCells.map { it.x }.toSet()
    return unmarkedRows.size < grid.height || unmarkedCols.size < grid.width
  }

  /** Returns the set of unmarked numbers in the board after marking the numbers. */
  fun unmarkedNumbers(numbers: List<Int>): Set<Int> = grid.grid.flatten().toSet() - numbers

  companion object {
    fun parse(boardStr: String): Board =
      Board(Grid(boardStr.lines().map { rowStr -> rowStr.ints() }))
  }
}

data class Input(val randomNumbers: List<Int>, val boards: List<Board>)

private typealias Output = Int

private val solution = object : Solution<Input, Output>(2021, "Day04") {
  override fun parse(input: String): Input {
    val (randomNumbersStr, boardsStr) = input.split("\n\n", limit = 2)
    val randomNumbers = randomNumbersStr.split(",").map { it.toInt() }
    val boards = boardsStr.split("\n\n").map { Board.parse(it) }

    return Input(randomNumbers, boards)
  }

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output {
    val winningBoard = when (part) {
      Part.PART1 -> input.boards.minBy { it.winningRound(input.randomNumbers) }
      Part.PART2 -> input.boards.maxBy { it.winningRound(input.randomNumbers) }
    }
    val winningRound = winningBoard.winningRound(input.randomNumbers)
    val unmarkedNumbers = winningBoard.unmarkedNumbers(input.randomNumbers.take(winningRound + 1))

    return unmarkedNumbers.sum() * input.randomNumbers[winningRound]
  }
}

fun main() = solution.run()
