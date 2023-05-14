@file:Suppress("PackageDirectoryMismatch")

package aoc22.day22

import lib.Direction
import lib.Point
import lib.Solution

enum class Tile(val symbol: Char) {
  WALL('#'), OPEN('.');

  companion object {
    fun parse(c: Char): Tile? = values().find { it.symbol == c }
  }
}

enum class Turn(val symbol: Char) {
  RIGHT('R'), LEFT('L');

  companion object {
    fun parse(c: Char): Turn? = values().find { it.symbol == c }
  }
}

fun Direction.turn(turn: Turn): Direction = when (turn) {
  Turn.RIGHT -> when (this) {
    Direction.RIGHT -> Direction.DOWN
    Direction.DOWN -> Direction.LEFT
    Direction.LEFT -> Direction.UP
    Direction.UP -> Direction.RIGHT
  }

  Turn.LEFT -> when (this) {
    Direction.RIGHT -> Direction.UP
    Direction.DOWN -> Direction.RIGHT
    Direction.LEFT -> Direction.DOWN
    Direction.UP -> Direction.LEFT
  }
}

data class Board(val tiles: Map<Point, Tile>) {
  private val xBoundary: Map<Int, IntRange> =
    tiles.keys.groupBy({ it.y }, { it.x }).mapValues { (_, xs) -> xs.min()..xs.max() }

  private val yBoundary: Map<Int, IntRange> =
    tiles.keys.groupBy({ it.x }, { it.y }).mapValues { (_, ys) -> ys.min()..ys.max() }

  fun getXBoundary(y: Int): IntRange = xBoundary[y] ?: error("Invalid y: $y")

  fun getYBoundary(x: Int): IntRange = yBoundary[x] ?: error("Invalid x: $x")

  companion object {
    fun parse(boardStr: String) = Board(boardStr.lines().flatMapIndexed(::parseLine).toMap())

    private fun parseLine(y: Int, s: String) = s.mapIndexedNotNull { x, c -> parseTile(x, y, c) }

    private fun parseTile(x: Int, y: Int, c: Char) = Tile.parse(c)?.let { Point(x, y) to it }
  }
}

sealed class MoveHandler(val board: Board) {
  abstract fun moveSingleStep(from: Point, direction: Direction): Pair<Point, Direction>

  fun move(from: Point, direction: Direction, steps: Int): Pair<Point, Direction> {
    var (currentPoint, currentDirection) = from to direction

    repeat(steps) {
      val (nextPoint, nextDirection) = moveSingleStep(currentPoint, currentDirection)
      if (board.tiles[nextPoint] != Tile.WALL) {
        currentPoint = nextPoint
        currentDirection = nextDirection
      }
    }

    return currentPoint to currentDirection
  }

  class WrappingMoveHandler(board: Board) : MoveHandler(board) {
    override fun moveSingleStep(from: Point, direction: Direction): Pair<Point, Direction> {
      val (x, y) = from
      val xBoundary = board.getXBoundary(y)
      val yBoundary = board.getYBoundary(x)

      var dest = from.move(direction)

      if (dest !in board.tiles) {
        dest = when (direction) {
          Direction.RIGHT -> Point(xBoundary.first, y)
          Direction.LEFT -> Point(xBoundary.last, y)
          Direction.UP -> Point(x, yBoundary.last)
          Direction.DOWN -> Point(x, yBoundary.first)
        }
      }

      return dest to direction
    }
  }
}

sealed interface Instruction {
  data class TurnInstruction(val turn: Turn) : Instruction
  data class MoveInstruction(val step: Int) : Instruction

  companion object {
    fun parse(instructionStr: String): Instruction =
      Turn.parse(instructionStr.first())
        ?.let(::TurnInstruction)
        ?: MoveInstruction(instructionStr.toInt())

    fun parseMany(instructionsStr: String): List<Instruction> =
      Regex("(\\d+|R|L)").findAll(instructionsStr).map { parse(it.value) }.toList()
  }
}

typealias Input = Pair<Board, List<Instruction>>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2022, "Day22") {
  override fun parse(input: String): Input {
    val (boardPart, instructionsPart) = input.split("\n\n")
    return Board.parse(boardPart) to Instruction.parseMany(instructionsPart)
  }

  override fun format(output: Output): String {
    return "$output"
  }

  override fun part1(input: Input): Output {
    val (board, instructions) = input
    val mover = MoveHandler.WrappingMoveHandler(board)

    var point = board.tiles
      .filterKeys { it.y == 0 }
      .filterValues { it == Tile.OPEN }
      .keys
      .min()
    var direction = Direction.RIGHT

    instructions.forEach { instruction ->
      when (instruction) {
        is Instruction.TurnInstruction -> direction = direction.turn(instruction.turn)
        is Instruction.MoveInstruction -> point =
          mover.move(point, direction, instruction.step).first
      }
    }

    return 1000 * (point.y + 1) + 4 * (point.x + 1) + direction.ordinal
  }

  override fun part2(input: Input): Output {
    TODO("Not yet implemented")
  }
}

fun main() = solution.run()
