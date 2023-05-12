@file:Suppress("PackageDirectoryMismatch")

package aoc22.day22

import lib.Point
import lib.Solution

enum class Tile {
  WALL, OPEN;

  companion object {
    fun parse(c: Char): Tile = when (c) {
      '#' -> WALL
      '.' -> OPEN
      else -> error("Invalid tile: $c")
    }
  }
}

enum class Direction {
  RIGHT, DOWN, LEFT, UP;

  fun turn(turn: Turn): Direction = when (turn) {
    Turn.RIGHT -> when (this) {
      RIGHT -> DOWN
      DOWN -> LEFT
      LEFT -> UP
      UP -> RIGHT
    }

    Turn.LEFT -> when (this) {
      RIGHT -> UP
      DOWN -> RIGHT
      LEFT -> DOWN
      UP -> LEFT
    }
  }
}

enum class Turn {
  RIGHT, LEFT;

  companion object {
    fun parse(c: Char): Turn = when (c) {
      'R' -> RIGHT
      'L' -> LEFT
      else -> error("Invalid turn: $c")
    }
  }
}

data class Board(val tiles: Map<Point, Tile>) {
  val width: Int = tiles.keys.map(Point::x).toSet().size
  val height: Int = tiles.keys.map(Point::y).toSet().size
  val xBoundary: Map<Int, IntRange> =
    tiles.keys.groupBy({ it.y }, { it.x }).mapValues { (_, xs) -> xs.min()..xs.max() }
  val yBoundary: Map<Int, IntRange> =
    tiles.keys.groupBy({ it.x }, { it.y }).mapValues { (_, ys) -> ys.min()..ys.max() }

  fun move(from: Point, direction: Direction): Point {
    val (x, y) = from
    val (l, r) = xBoundary[y]?.let { it.first to it.last } ?: error("Invalid y: $y")
    val (u, d) = yBoundary[x]?.let { it.first to it.last } ?: error("Invalid x: $x")
    val dest = when (direction) {
      Direction.RIGHT -> if (x < r) Point(x + 1, y) else Point(l, y)
      Direction.LEFT -> if (x > l) Point(x - 1, y) else Point(r, y)
      Direction.UP -> if (y > u) Point(x, y - 1) else Point(x, d)
      Direction.DOWN -> if (y < d) Point(x, y + 1) else Point(x, u)
    }
    return if (tiles[dest] == Tile.WALL) from else dest
  }

  fun move(from: Point, direction: Direction, steps: Int): Point {
    var dest = from
    repeat(steps) {
      dest = move(dest, direction)
    }
    return dest
  }

  companion object {
    fun parse(boardStr: String): Board = Board(
      boardStr.lines().flatMapIndexed { y, line ->
        line.mapIndexedNotNull { x, c ->
          when (c) {
            '#' -> Tile.WALL
            '.' -> Tile.OPEN
            else -> null
          }?.let {
            Point(x, y) to it
          }
        }
      }.toMap()
    )
  }
}

sealed interface Instruction {
  data class TurnInstruction(val turn: Turn) : Instruction
  data class MoveInstruction(val step: Int) : Instruction

  companion object {
    fun parse(instructionStr: String): Instruction = when (instructionStr) {
      "R" -> TurnInstruction(Turn.RIGHT)
      "L" -> TurnInstruction(Turn.LEFT)
      else -> MoveInstruction(instructionStr.toInt())
    }

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
    var point = board.tiles
      .filterKeys { it.y == 0 }
      .filterValues { it == Tile.OPEN }
      .keys
      .min()
    var direction = Direction.RIGHT

    instructions.forEach { instruction ->
      when (instruction) {
        is Instruction.TurnInstruction -> direction = direction.turn(instruction.turn)
        is Instruction.MoveInstruction -> point = board.move(point, direction, instruction.step)
      }
    }

    return 1000 * (point.y + 1) + 4 * (point.x + 1) + direction.ordinal
  }

  override fun part2(input: Input): Output {
    TODO("Not yet implemented")
  }
}

fun main() = solution.run()
