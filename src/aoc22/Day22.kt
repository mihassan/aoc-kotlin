@file:Suppress("PackageDirectoryMismatch")

package aoc22.day22

import kotlin.math.sqrt
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
          Direction.DOWN -> Point(x, yBoundary.first)
          Direction.LEFT -> Point(xBoundary.last, y)
          Direction.UP -> Point(x, yBoundary.last)
        }
      }

      return dest to direction
    }
  }

  /**
   * This move handler assumes that the board is a cube with a fixed layout:
   *
   *    | F^ | R^
   * ---|----|---
   *    | D^ |
   * ---|----|---
   * Lv | Bv |
   * ---|----|---
   * U> |    |
   *
   * Also see: https://imgur.com/cGEtOzf
   */
  class CubeMoveHandler(board: Board) : MoveHandler(board) {
    private val side = sqrt(board.tiles.size / 6.0).toInt()

    override fun moveSingleStep(from: Point, direction: Direction): Pair<Point, Direction> {
      val block = from / side
      val (blockX, blockY) = from - block * side

      return when (direction) {
        Direction.RIGHT -> handleRight(from, block, blockX, blockY)
        Direction.DOWN -> handleDown(from, block, blockX, blockY)
        Direction.LEFT -> handleLeft(from, block, blockX, blockY)
        Direction.UP -> handleUp(from, block, blockX, blockY)
      }
    }

    private fun handleRight(
      from: Point,
      block: Point,
      blockX: Int,
      blockY: Int,
    ): Pair<Point, Direction> {
      if (blockX < side - 1) {
        return from.right() to Direction.RIGHT
      }
      return when (block) {
        // F -> R
        Point(1, 0) -> Point(2 * side, blockY) to Direction.RIGHT
        // R -> B
        Point(2, 0) -> Point(side + (side - 1), 2 * side + (side - blockY - 1)) to Direction.LEFT
        // D -> R
        Point(1, 1) -> Point(2 * side + blockY, (side - 1)) to Direction.UP
        // L -> B
        Point(0, 2) -> Point(side, 2 * side + blockY) to Direction.RIGHT
        // B -> R
        Point(1, 2) -> Point(2 * side + (side - 1), side - blockY - 1) to Direction.LEFT
        // U -> B
        Point(0, 3) -> Point(side + blockY, 2 * side + (side - 1)) to Direction.UP
        else -> error("Invalid block: $block")
      }
    }


    private fun handleDown(
      from: Point,
      block: Point,
      blockX: Int,
      blockY: Int,
    ): Pair<Point, Direction> {
      if (blockY < side - 1) {
        return from.down() to Direction.DOWN
      }
      return when (block) {
        // F -> D
        Point(1, 0) -> Point(side + blockX, side) to Direction.DOWN
        // R -> D
        Point(2, 0) -> Point(side + (side - 1), side + blockX) to Direction.LEFT
        // D -> B
        Point(1, 1) -> Point(side + blockX, 2 * side) to Direction.DOWN
        // L -> U
        Point(0, 2) -> Point(blockX, 3 * side) to Direction.DOWN
        // B -> U
        Point(1, 2) -> Point((side - 1), 3 * side + blockX) to Direction.LEFT
        // U -> R
        Point(0, 3) -> Point(2 * side + blockX, 0) to Direction.DOWN
        else -> error("Invalid block: $block")
      }
    }

    private fun handleLeft(
      from: Point,
      block: Point,
      blockX: Int,
      blockY: Int,
    ): Pair<Point, Direction> {
      if (blockX > 0) {
        return from.left() to Direction.LEFT
      }
      return when (block) {
        // F -> L
        Point(1, 0) -> Point(0, 2 * side + (side - blockY - 1)) to Direction.RIGHT
        // R -> F
        Point(2, 0) -> Point(side + (side - 1), blockY) to Direction.LEFT
        // D -> L
        Point(1, 1) -> Point(blockY, 2 * side) to Direction.DOWN
        // L -> F
        Point(0, 2) -> Point(side, (side - blockY - 1)) to Direction.RIGHT
        // B -> L
        Point(1, 2) -> Point((side - 1), 2 * side + blockY) to Direction.LEFT
        // U -> F
        Point(0, 3) -> Point(side + blockY, 0) to Direction.DOWN
        else -> error("Invalid block: $block")
      }
    }

    private fun handleUp(
      from: Point,
      block: Point,
      blockX: Int,
      blockY: Int,
    ): Pair<Point, Direction> {
      if (blockY > 0) {
        return from.up() to Direction.UP
      }
      return when (block) {
        // F -> U
        Point(1, 0) -> Point(0, 3 * side + blockX) to Direction.RIGHT
        // R -> U
        Point(2, 0) -> Point(blockX, 3 * side + (side - 1)) to Direction.UP
        // D -> F
        Point(1, 1) -> Point(side + blockX, (side - 1)) to Direction.UP
        // L -> D
        Point(0, 2) -> Point(side, side + blockX) to Direction.RIGHT
        // B -> D
        Point(1, 2) -> Point(side + blockX, side + (side - 1)) to Direction.UP
        // U -> L
        Point(0, 3) -> Point(blockX, 2 * side + (side - 1)) to Direction.UP
        else -> error("Invalid block: $block")
      }
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

  override fun solve(part: Part, input: Input): Output {
    val (board, instructions) = input
    val mover = when(part) {
      Part.PART1 -> MoveHandler.WrappingMoveHandler(board)
      Part.PART2 -> MoveHandler.CubeMoveHandler(board)
    }

    var point = board.tiles
      .filterKeys { it.y == 0 }
      .filterValues { it == Tile.OPEN }
      .keys
      .min()
    var direction = Direction.RIGHT

    instructions.forEach { instruction ->
      when (instruction) {
        is Instruction.TurnInstruction -> direction = direction.turn(instruction.turn)
        is Instruction.MoveInstruction -> {
          val move = mover.move(point, direction, instruction.step)
          point = move.first
          direction = move.second
        }
      }
    }

    return 1000 * (point.y + 1) + 4 * (point.x + 1) + direction.ordinal
  }
}

fun main() = solution.run()
