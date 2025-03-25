@file:Suppress("PackageDirectoryMismatch")

package aoc21.day02

import lib.Point
import lib.Solution
import lib.Strings.words

private enum class CommandType {
  Forward, Down, Up;

  companion object {
    fun parse(commandTypeStr: String): CommandType =
      entries.find { it.name.lowercase() == commandTypeStr }
        ?: error("Invalid command: $commandTypeStr")
  }
}

private data class Command(val type: CommandType, val unit: Int) {
  fun apply(point: Point): Point = when (type) {
    CommandType.Forward -> point.copy(x = point.x + unit)
    CommandType.Down -> point.copy(y = point.y + unit)
    CommandType.Up -> point.copy(y = point.y - unit)
  }

  fun apply(point: Point, aim: Int): Pair<Point, Int> = when (type) {
    CommandType.Forward -> Point(x = point.x + unit, y = point.y + aim * unit) to aim
    CommandType.Down -> point to aim + unit
    CommandType.Up -> point to aim - unit
  }

  companion object {
    fun parse(commandStr: String): Command {
      val (typeStr, unitStr) = commandStr.words()
      return Command(CommandType.parse(typeStr), unitStr.toInt())
    }
  }
}

private typealias Input = List<Command>

private typealias Output = Int

private val solution = object : Solution<Input, Output>(2021, "Day02") {
  override fun parse(input: String): Input = input.lines().map { Command.parse(it) }

  override fun format(output: Int): String = "$output"

  override fun part1(input: Input): Output {
    val initialPoint = Point(0, 0)
    val finalPoint = input.fold(initialPoint) { point, command -> command.apply(point) }
    return finalPoint.x * finalPoint.y
  }

  override fun part2(input: Input): Output {
    val initialPoint = Point(0, 0)
    val initialAim = 0
    val (finalPoint, _) = input.fold(initialPoint to initialAim) { (point, aim), command ->
      command.apply(point, aim)
    }
    return finalPoint.x * finalPoint.y
  }
}

fun main() = solution.run()
