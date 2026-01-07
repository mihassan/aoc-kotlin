@file:Suppress("PackageDirectoryMismatch")

package aoc25.day10

import lib.Combinatorics.allSubSets
import lib.ProblemInput
import lib.Solution
import lib.Strings.extractInts
import lib.Strings.words

/**
 * Represents the target state of indicator lights.
 * Each light can be either on (#) or off (.).
 */
private data class IndicatorDiagram(val targetLights: List<Boolean>) {
  companion object {
    fun parse(str: String): IndicatorDiagram = IndicatorDiagram(str.map { it == '#' })
  }
}

/**
 * A button that toggles specific indicator lights when pressed.
 * @property affectedIndices The indices of lights that this button toggles
 */
private data class Button(val affectedIndices: List<Int>) {
  companion object {
    fun parse(buttonStr: String): Button = Button(buttonStr.extractInts())
  }
}

/**
 * Joltage requirements for the machine.
 * Each requirement represents the target joltage level for a specific component.
 */
private data class Joltage(val requirements: List<Int>) {
  companion object {
    fun parse(joltageStr: String): Joltage = Joltage(joltageStr.extractInts())
  }
}

/**
 * Represents a machine with indicator lights and buttons.
 * This is a "lights out" style puzzle where we need to find the minimum
 * number of button presses to achieve the target indicator configuration.
 */
private data class Machine(
  val indicatorDiagram: IndicatorDiagram,
  val buttons: List<Button>,
  val joltage: Joltage,
) {
  /**
   * Finds the minimum number of button presses needed to configure indicators correctly.
   * Uses brute force enumeration of all possible button combinations.
   */
  fun findMinimumButtonPresses(): Long {
    return allSubSets(buttons.toSet()).filter { buttonSet -> buttonSet.producesTargetConfiguration() }
      .minOf { it.size.toLong() }
  }

  /**
   * Simulates pressing a set of buttons and checks if it produces the target configuration.
   * Each button toggles specific lights, and pressing the same button twice cancels out.
   */
  private fun Set<Button>.producesTargetConfiguration(): Boolean =
    simulateButtonPresses() == indicatorDiagram.targetLights

  /**
   * Simulates pressing all buttons in this set and returns the final light states.
   */
  private fun Set<Button>.simulateButtonPresses(): List<Boolean> {
    val states = MutableList(indicatorDiagram.targetLights.size) { false }
    for (button in this) {
      for (index in button.affectedIndices) {
        states[index] = !states[index]
      }
    }
    return states
  }

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

      return Machine(
        indicatorDiagram = IndicatorDiagram.parse(lightsGroup),
        buttons = buttonsGroup.words().map(Button::parse),
        joltage = Joltage.parse(requirementsGroup)
      )
    }
  }
}

private typealias Input = List<Machine>
private typealias Output = Long

/**
 * Cost multiplier when recursively solving halved requirements.
 * Each level of recursion doubles the effective cost of button presses.
 */
private const val RECURSION_COST_MULTIPLIER = 2

private val solution = object : Solution<Input, Output>(2025, "Day10") {
  override fun parse(input: ProblemInput): Input = input.linesAs(Machine::parse)

  override fun format(output: Output): String = output.toString()

  override fun part1(input: Input): Output = input.sumOf { it.findMinimumButtonPresses() }

  /**
   * Finds minimum button presses to match joltage requirements using recursive memoized search.
   *
   * Algorithm:
   * 1. Base case: all requirements are 0 → return 0
   * 2. Impossible case: any requirement is negative → return MAX_VALUE
   * 3. Find button subsets matching the parity pattern (requirements mod 2)
   * 4. Recursively solve for halved requirements after pressing those buttons
   * 5. Return minimum across all valid button combinations
   */
  override fun part2(input: Input): Output {
    return input.sumOf { machine ->
      memoizationCache.clear()
      machine.buttons.findMinimumButtonPresses(machine.joltage.requirements)
    }
  }

  private val memoizationCache = mutableMapOf<List<Int>, Long>()

  private fun List<Button>.findMinimumButtonPresses(requirements: List<Int>): Long {
    memoizationCache[requirements]?.let { return it }

    if (requirements.all { it == 0 }) return 0L
    if (requirements.any { it < 0 }) return Long.MAX_VALUE

    val targetPattern = requirements.map { it % 2 == 1 }

    val minButtonPresses =
      allSubSets(toSet()).filter { it.hasParityPattern(targetPattern) }.minOfOrNull { buttonSet ->
        val newRequirements = buttonSet.decrementRequirements(requirements)
        val halvedRequirements = newRequirements.map { it / 2 }
        val recursiveCost = findMinimumButtonPresses(halvedRequirements)
        if (recursiveCost == Long.MAX_VALUE) {
          Long.MAX_VALUE
        } else {
          RECURSION_COST_MULTIPLIER * recursiveCost + buttonSet.size
        }
      } ?: Long.MAX_VALUE

    return minButtonPresses.also { memoizationCache[requirements] = it }
  }

  /**
   * Applies button toggles to create a boolean pattern.
   * This is the core toggle logic used by both light simulation and parity checking.
   */
  private fun Set<Button>.applyToggles(size: Int): List<Boolean> {
    val states = MutableList(size) { false }
    for (button in this) {
      for (index in button.affectedIndices) {
        states[index] = !states[index]
      }
    }
    return states
  }

  /**
   * Checks if pressing this set of buttons produces the target parity pattern.
   * Each button toggles indices, and we check if the final parity matches the target.
   */
  private fun Set<Button>.hasParityPattern(targetPattern: List<Boolean>): Boolean {
    return applyToggles(targetPattern.size) == targetPattern
  }

  /**
   * Decrements requirements at indices affected by this set of buttons.
   * Returns a new list with decremented values.
   */
  private fun Set<Button>.decrementRequirements(currentRequirements: List<Int>): List<Int> {
    val newRequirements = currentRequirements.toMutableList()
    for (button in this) {
      for (index in button.affectedIndices) {
        newRequirements[index] -= 1
      }
    }
    return newRequirements
  }
}

fun main() = solution.run()
