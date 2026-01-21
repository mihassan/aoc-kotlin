@file:Suppress("PackageDirectoryMismatch")

package aoc25.day10

import lib.Combinatorics.allSubSets
import lib.ProblemInput
import lib.Solution
import lib.Strings.extractInts
import lib.Strings.words

private data class Machine(
  val lights: List<Boolean>,
  val buttons: List<List<Boolean>>,
  val requirements: List<Int>,
) {
  companion object {
    // Matches format: [lights] (button1) (button2) ... {requirements}
    private val MACHINE_REGEX =
      Regex("""\[(?<lights>.*?)]\s*(?<buttons>(\([^)]*\)\s*)+)\s*\{(?<requirements>.*?)}""")

    fun parse(machineStr: String): Machine {
      val match =
        MACHINE_REGEX.matchEntire(machineStr) ?: error("Invalid machine format: $machineStr")

      val lightsGroup = match.groups["lights"]?.value ?: error("Missing lights group")
      val buttonsGroup = match.groups["buttons"]?.value ?: error("Missing buttons group")
      val requirementsGroup =
        match.groups["requirements"]?.value ?: error("Missing requirements group")

      val lights = lightsGroup.map { it == '#' }

      val buttons = mutableListOf<List<Boolean>>()
      for (group in buttonsGroup.words()) {
        val pressed = MutableList(lights.size) { false }
        for (button in group.extractInts()) {
          pressed[button] = true
        }
        buttons.add(pressed)
      }

      val requirements = requirementsGroup.extractInts()

      return Machine(
        lights = lights,
        buttons = buttons,
        requirements = requirements,
      )
    }
  }
}

private typealias Input = List<Machine>
private typealias Output = Long

private val solution = object : Solution<Input, Output>(2025, "Day10") {
  override fun parse(input: ProblemInput): Input = input.linesAs(Machine::parse)

  override fun format(output: Output): String = output.toString()

  override fun part1(input: Input): Output =
    input.sumOf { machine -> solveLights(machine.buttons, machine.lights) }

  private fun solveLights(buttons: List<List<Boolean>>, targetLights: List<Boolean>): Long {
    val size = targetLights.size
    var minPresses = Long.MAX_VALUE

    for (buttonSubset in allSubSets(buttons)) {
      val resultingLights = MutableList(size) { false }
      for (button in buttonSubset) {
        for (index in button.indices) {
          if (button[index]) {
            resultingLights[index] = !resultingLights[index]
          }
        }
      }
      if (resultingLights == targetLights) {
        minPresses = minOf(minPresses, buttonSubset.size.toLong())
      }
    }

    return minPresses
  }

  override fun part2(input: Input): Output = input.sumOf { machine ->
    solveRequirements(machine.buttons, machine.requirements)
  }

  /**
   * Finds minimum button presses to match requirements using recursive memoized search.
   *
   * Algorithm:
   * 1. Base case: all requirements are 0 → return 0
   * 2. Impossible case: any requirement is negative → return MAX_VALUE
   * 3. Find button subsets matching the parity pattern (requirements mod 2)
   * 4. Recursively solve for halved requirements after pressing those buttons
   * 5. Return minimum across all valid button combinations
   */
  private fun solveRequirements(buttons: List<List<Boolean>>, requirements: List<Int>): Long {
    val size = requirements.size

    // Precalculate impact of every button subset
    // Group them by their parity pattern (mod 2) to quickly find valid candidates for LSB
    val impactByParity: MutableMap<List<Boolean>, MutableList<Pair<Int, List<Int>>>> = mutableMapOf()

    for (buttonSubset in allSubSets(buttons)) {
      val subsetImpact = MutableList(size) { 0 }

      for (button in buttonSubset) {
        for ((index, pressed) in button.withIndex()) {
          if (pressed) {
            subsetImpact[index] += 1
          }
        }
      }

      val parityPattern = subsetImpact.map { it % 2 == 1 }

      impactByParity
        .getOrPut(parityPattern) { mutableListOf() }
        .add(buttonSubset.size to subsetImpact)
    }

    val memo: MutableMap<List<Int>, Long> = mutableMapOf()

    fun findMinPresses(currentReqs: List<Int>): Long {
      if (currentReqs.all { it == 0 }) return 0L
      // If we overshot (negative requirements), this path is invalid
      if (currentReqs.any { it < 0 }) return Long.MAX_VALUE

      memo[currentReqs]?.let { return it }

      val currentParity = currentReqs.map { it % 2 != 0 }
      var minTotalPresses = Long.MAX_VALUE

      // Try all subsets that match the parity of the current requirements
      val candidates = impactByParity[currentParity] ?: emptyList()

      for ((subsetSize, impact) in candidates) {
        val nextReqs = List(size) { index ->
          (currentReqs[index] - impact[index]) / 2
        }

        val costRest = findMinPresses(nextReqs)
        if (costRest != Long.MAX_VALUE) {
          // Current level costs 'subsetSize', higher levels cost '2 * costRest'
          val totalCost = 2 * costRest + subsetSize
          if (totalCost < minTotalPresses) {
            minTotalPresses = totalCost
          }
        }
      }

      memo[currentReqs] = minTotalPresses
      return minTotalPresses
    }

    return findMinPresses(requirements)
  }
}

fun main() = solution.run()
