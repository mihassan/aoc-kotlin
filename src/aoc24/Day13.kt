@file:Suppress("PackageDirectoryMismatch")

package aoc24.day13

import kotlin.math.floor
import lib.Solution

enum class ButtonType(val representation: Char, val cost: Long) {
  A('A', 3),
  B('B', 1);

  companion object {
    fun parse(char: Char): ButtonType = values().first { it.representation == char }
  }
}

data class Button(val type: ButtonType, val x: Long, val y: Long) {
  companion object {
    private val BUTTON_REGEX = """Button ([A-Z]): X\+(\d+), Y\+(\d+)""".toRegex()

    fun parse(buttonStr: String): Button {
      val (type, x, y) = BUTTON_REGEX.matchEntire(buttonStr)?.destructured
        ?: error("Invalid button string: $buttonStr")
      return Button(ButtonType.parse(type.first()), x.toLong(), y.toLong())
    }
  }
}

data class Prize(val x: Long, val y: Long) {
  fun move(dx: Long, dy: Long): Prize = Prize(x + dx, y + dy)

  companion object {
    private val PRIZE_REGEX = """Prize: X=(\d+), Y=(\d+)""".toRegex()

    fun parse(prizeStr: String): Prize {
      val (x, y) = PRIZE_REGEX.matchEntire(prizeStr)?.destructured
        ?: error("Invalid prize string: $prizeStr")
      return Prize(x.toLong(), y.toLong())
    }
  }
}

data class Machine(val buttonA: Button, val buttonB: Button, val prize: Prize) {
  companion object {
    fun parse(machineStr: String): Machine {
      val (buttonA, buttonB, prize) = machineStr.split("\n")
      return Machine(Button.parse(buttonA), Button.parse(buttonB), Prize.parse(prize))
    }
  }
}

typealias Input = List<Machine>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2024, "Day13") {
  override fun parse(input: String): Input = input.split("\n\n").map(Machine::parse)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.sumOf { (buttonA, buttonB, prize) ->
    val denom = (buttonA.x * buttonB.y - buttonA.y * buttonB.x).toDouble()
    require(denom != 0.0) {
      "ButtonA and ButtonB are linearly dependant, i.e., there are many solutions."
    }

    val a = (prize.x * buttonB.y - prize.y * buttonB.x) / denom
    val b = (prize.y * buttonA.x - prize.x * buttonA.y) / denom

    val found = floor(a) == a && floor(b) == b && a >= 0 && b >= 0
    val tokens = buttonA.type.cost * a.toLong() + buttonB.type.cost * b.toLong()

    if (found) tokens else 0L
  }

  override fun part2(input: Input): Output =
    part1(input.map { (buttonA, buttonB, prize) ->
      Machine(buttonA, buttonB, prize.move(10000000000000L, 10000000000000L))
    })
}

fun main() = solution.run()
