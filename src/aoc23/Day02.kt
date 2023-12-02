@file:Suppress("PackageDirectoryMismatch")

package aoc23.day02

import java.lang.Integer.max
import lib.Solution

enum class Cube {
  RED, GREEN, BLUE;

  companion object {
    fun parse(cubeStr: String) = Cube.valueOf(cubeStr.uppercase())
  }
}

data class Cubes(val cubes: Map<Cube, Int>) {
  fun isPossible(maxCubes: Cubes) = cubes.all { (cube, count) ->
    count <= (maxCubes.cubes[cube] ?: Int.MIN_VALUE)
  }

  fun power() = Cube.values().fold(1) { acc, cube -> acc * (cubes[cube] ?: 0) }

  companion object {
    fun parse(cubesStr: String) = cubesStr.split(", ").associate {
      val (countPart, cubePart) = it.split(" ")
      Cube.parse(cubePart) to countPart.toInt()
    }.let {
      Cubes(it)
    }
  }
}

data class Game(val id: Int, val cubesList: List<Cubes>) {
  fun isPossible(maxCubes: Cubes) = cubesList.all { it.isPossible(maxCubes) }

  fun power() = cubesList
    .flatMap { it.cubes.entries }
    .groupingBy { it.key }
    .fold(0) { acc, (_, count) -> max(acc, count) }
    .let { Cubes(it) }
    .power()

  companion object {
    fun parse(gameStr: String): Game {
      val (idPart, rest) = gameStr.split(": ")

      val id = idPart.substringAfter("Game ").toInt()
      val cubesList = rest.split("; ").map { Cubes.parse(it) }

      return Game(id, cubesList)
    }
  }
}

typealias Input = List<Game>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2023, "Day02") {
  override fun parse(input: String): Input = input.lines().map { Game.parse(it) }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.filter { it.isPossible(MAX_CUBES) }.sumOf { it.id }

  override fun part2(input: Input): Output = input.sumOf { it.power() }

  val MAX_CUBES = Cubes(mapOf(Cube.RED to 12, Cube.GREEN to 13, Cube.BLUE to 14))
}

fun main() = solution.run()
