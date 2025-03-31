@file:Suppress("PackageDirectoryMismatch")

package aoc21.day11

import lib.Adjacency
import lib.Grid
import lib.Point
import lib.Solution

private typealias Input = Grid<Int>

private typealias Output = Int

private class Simulation(val grid: Grid<Int>) {
  var totalFlashes: Int = 0
    private set
  var newFlashesDuringLastStep: Int = 0
    private set

  private val energyLevels: MutableMap<Point, Int> =
    grid.indices().associateWith { grid[it]!! }.toMutableMap()

  fun step() {
    // Keep track of which cells have flashed during this step
    val flashedDuringStep = mutableSetOf<Point>()

    incrementEnergyLevels()

    while (true) {
      val newlyFlashed = checkForNewFlashes(flashedDuringStep)
      if (newlyFlashed.isEmpty()) break
      flashedDuringStep += newlyFlashed

      // Increment energy levels of all cells around the newly flashed cells
      incrementEnergyLevelsAroundFlash(newlyFlashed)
    }

    // Reset energy levels of all cells that flashed
    resetEnergyLevels(flashedDuringStep)

    // Record the flashes that occurred during this step
    newFlashesDuringLastStep = flashedDuringStep.size
    totalFlashes += newFlashesDuringLastStep
  }

  private fun resetEnergyLevels(flashes: MutableSet<Point>) {
    flashes.forEach { energyLevels[it] = 0 }
  }

  private fun incrementEnergyLevelsAroundFlash(flashes: Set<Point>) {
    flashes.flatMap { it.adjacents(Adjacency.ALL) }.forEach {
      if (it in energyLevels) {
        energyLevels[it] = energyLevels[it]!! + 1
      }
    }
  }

  private fun checkForNewFlashes(flashedDuringStep: MutableSet<Point>): Set<Point> =
    energyLevels.filter { it.value > 9 }.keys - flashedDuringStep

  private fun incrementEnergyLevels() {
    energyLevels.keys.toList().forEach { energyLevels[it] = energyLevels[it]!! + 1 }
  }
}

private val solution = object : Solution<Input, Output>(2021, "Day11") {
  override fun parse(input: String): Input = Grid.parse(input).map { it.digitToInt() }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    val simulation = Simulation(input)
    repeat(100) { simulation.step() }
    return simulation.totalFlashes
  }

  override fun part2(input: Input): Output {
    val totalCells = input.width * input.height
    val simulation = Simulation(input)
    var step = 0
    while (true) {
      step++
      simulation.step()
      if (simulation.newFlashesDuringLastStep == totalCells) break
    }
    return step
  }
}

fun main() = solution.run()
