@file:Suppress("PackageDirectoryMismatch")

package aoc24.day08

import lib.Combinatorics.combinations
import lib.Grid
import lib.Maths.gcd
import lib.Point
import lib.Solution

typealias Frequency = Char
typealias Antenna = Point
typealias Antinode = Point

data class Roof(
  val height: Int,
  val width: Int,
  val antennasGroupedByFrequency: Map<Frequency, Set<Antenna>>,
) {
  val frequencies: Set<Frequency>
    get() = antennasGroupedByFrequency.keys

  operator fun contains(antenna: Antenna): Boolean =
    antenna.x in 0..<width && antenna.y in 0..<height

  fun closeAntinodes(frequency: Frequency): Set<Antinode> =
    combinations(
      antennasGroupedByFrequency[frequency] ?: emptySet(), 2
    ).map {
      closeAntinodesForTwoAntennas(it.first(), it.last())
    }.flatten().toSet()

  fun allAntinodes(frequency: Frequency): Set<Antinode> =
    combinations(
      antennasGroupedByFrequency[frequency] ?: emptySet(), 2
    ).map {
      allAntinodesForTwoAntennas(it.first(), it.last())
    }.flatten().toSet()

  private fun closeAntinodesForTwoAntennas(antenna1: Antenna, antenna2: Antenna): Set<Antinode> =
    setOf(antenna1 * 2 - antenna2, antenna2 * 2 - antenna1).filter { it in this }.toSet()

  private fun allAntinodesForTwoAntennas(antenna1: Antenna, antenna2: Antenna): Set<Antinode> {
    val (dX, dY) = antenna1 - antenna2
    val g = dX gcd dY
    val step = Point(dX / g, dY / g)

    val antinodes1 = generateSequence(antenna1) {
      val next = it + step
      if (next in this) next else null
    }

    val antinodes2 = generateSequence(antenna1) {
      val next = it - step
      if (next in this) next else null
    }

    return (antinodes1 + antinodes2).toSet()
  }

  companion object {
    fun parse(roofStr: String): Roof {
      val grid = Grid.parse(roofStr)

      val antennas = buildMap<Frequency, MutableSet<Antenna>> {
        grid.forEachIndexed { antenna, frequency ->
          if (frequency != '.')
            getOrPut(frequency) { mutableSetOf() }.add(antenna)
        }
      }

      return Roof(grid.height, grid.width, antennas)
    }
  }
}

typealias Input = Roof

typealias Output = Int

private val solution = object : Solution<Input, Output>(2024, "Day08") {
  override fun parse(input: String): Input = Roof.parse(input)

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output =
    input.frequencies.map {
      when (part) {
        Part.PART1 -> input.closeAntinodes(it)
        Part.PART2 -> input.allAntinodes(it)
      }
    }
      .flatten()
      .toSet()
      .size
}

fun main() = solution.run()
