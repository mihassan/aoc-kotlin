@file:Suppress("PackageDirectoryMismatch")

package aoc25.day07

import lib.Grid
import lib.Point
import lib.Solution

enum class Cell(val symbol: Char) {
  EMPTY('.'), START('S'), SPLITTER('^'), BEAM('|');

  companion object {
    fun parse(cellChar: Char): Cell =
      entries.firstOrNull { it.symbol == cellChar }
        ?: error("Unknown cell: $cellChar")
  }
}

typealias Input = Grid<Cell>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2025, "Day07") {
  override fun parse(input: String): Input = Grid.parse(input).map(Cell::parse)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    var splitCount = 0L
    val activeBeams = mutableSetOf<Point>()

    input.forEachIndexed { point, cell ->
      when (cell) {
        Cell.START -> activeBeams += point.down()
        Cell.EMPTY -> if (point.up() in activeBeams) activeBeams += point
        Cell.SPLITTER -> {
          if (point.up() in activeBeams) {
            splitCount++
            activeBeams += point.left()
            activeBeams += point.right()
          }
        }

        Cell.BEAM -> Unit
      }
    }

    return splitCount
  }

  override fun part2(input: Input): Output {
    val timelineCount = mutableMapOf<Point, Long>().withDefault { 0L }

    input.forEachIndexed { point, cell ->
      val incomingTimelines = timelineCount.getValue(point.up())

      when (cell) {
        Cell.START -> timelineCount[point.down()] = 1L
        Cell.EMPTY -> timelineCount.addTo(point, incomingTimelines)
        Cell.SPLITTER -> {
          timelineCount.addTo(point.left(), incomingTimelines)
          timelineCount.addTo(point.right(), incomingTimelines)
        }

        Cell.BEAM -> Unit
      }
    }

    val lastRowIndex = input.height - 1
    return timelineCount
      .filterKeys { it.y == lastRowIndex }
      .values
      .sum()
  }

  private fun MutableMap<Point, Long>.addTo(key: Point, value: Long) {
    this[key] = getValue(key) + value
  }
}

fun main() = solution.run()
