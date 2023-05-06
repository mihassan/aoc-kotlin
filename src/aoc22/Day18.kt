@file:Suppress("PackageDirectoryMismatch")

package aoc22.day18

import lib.Solution

data class Cube(val x: Int, val y: Int, val z: Int) {
  fun adjacentCubes(): Set<Cube> = setOf(
    Cube(x - 1, y, z),
    Cube(x + 1, y, z),
    Cube(x, y - 1, z),
    Cube(x, y + 1, z),
    Cube(x, y, z - 1),
    Cube(x, y, z + 1)
  )

  companion object {
    fun parse(str: String): Cube {
      val (x, y, z) = str.split(",").map(String::toInt)
      return Cube(x, y, z)
    }
  }
}

data class Bound3D(val xRange: IntRange, val yRange: IntRange, val zRange: IntRange) {
  operator fun contains(cube: Cube): Boolean =
    cube.x in xRange && cube.y in yRange && cube.z in zRange

  fun growBy(amount: Int): Bound3D =
    Bound3D(xRange.growBy(amount), yRange.growBy(amount), zRange.growBy(amount))

  fun cubesOnBorder(): Set<Cube> = buildSet {
    xRange.forEach { x ->
      yRange.forEach { y ->
        add(Cube(x, y, zRange.first))
        add(Cube(x, y, zRange.last))
      }
    }
    yRange.forEach { y ->
      zRange.forEach { z ->
        add(Cube(xRange.first, y, z))
        add(Cube(xRange.last, y, z))
      }
    }
    zRange.forEach { z ->
      xRange.forEach { x ->
        add(Cube(x, yRange.first, z))
        add(Cube(x, yRange.last, z))
      }
    }
  }

  private fun IntRange.growBy(amount: Int): IntRange = first - amount..last + amount

  companion object {
    fun of(cubes: Set<Cube>): Bound3D {
      val xRange = cubes.minOf { it.x }..cubes.maxOf { it.x }
      val yRange = cubes.minOf { it.y }..cubes.maxOf { it.y }
      val zRange = cubes.minOf { it.z }..cubes.maxOf { it.z }
      return Bound3D(xRange, yRange, zRange)
    }
  }
}

typealias Input = Set<Cube>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2022, "Day18") {
  override fun parse(input: String): Input = input.lines().map { Cube.parse(it) }.toSet()

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    return input.sumOf { cube ->
      cube.adjacentCubes().filter { it !in input }.size
    }
  }

  override fun part2(input: Input): Output {
    val bound = Bound3D.of(input).growBy(1)
    val cubesToVisit = bound.cubesOnBorder().toMutableSet()
    val visitedCubes = mutableSetOf<Cube>()
    var result = 0

    while (cubesToVisit.isNotEmpty()) {
      val cube = cubesToVisit.first()
      visitedCubes.add(cube)
      cube.adjacentCubes().forEach { nextCube ->
        if (nextCube in input) {
          result += 1
        } else if (nextCube in bound && nextCube !in visitedCubes && nextCube !in cubesToVisit) {
          cubesToVisit.add(nextCube)
        }
      }
      cubesToVisit.remove(cube)
    }
    return result
  }
}

fun main() = solution.run()
