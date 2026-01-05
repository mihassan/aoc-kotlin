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
    fun parse(str: String): IndicatorDiagram =
      IndicatorDiagram(str.map { it == '#' })
  }
}

/**
 * A button that toggles specific indicator lights when pressed.
 * @property affectedIndices The indices of lights that this button toggles
 */
private data class Button(val affectedIndices: List<Int>) {
  companion object {
    fun parse(buttonStr: String): Button =
      Button(buttonStr.extractInts())
  }
}

/**
 * Joltage requirements for the machine (currently unused in logic).
 */
private data class Joltage(val requirements: List<Int>) {
  companion object {
    fun parse(joltageStr: String): Joltage =
      Joltage(joltageStr.extractInts())
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
   *
   * Note: This has O(2^n) complexity where n is the number of buttons.
   * For production code with >20 buttons, consider using Gaussian elimination
   * over GF(2) for O(n^3) complexity instead.
   */
  fun findMinimumButtonPresses(): Long {
    return allSubSets(buttons.toSet())
      .filter { buttonSet -> buttonSet.producesTargetConfiguration() }
      .minOf { it.size.toLong() }
  }

  /**
   * Simulates pressing a set of buttons and checks if it produces the target configuration.
   * Each button toggles specific lights, and pressing the same button twice cancels out.
   */
  private fun Set<Button>.producesTargetConfiguration(): Boolean {
    val currentState = simulateButtonPresses()
    return currentState == indicatorDiagram.targetLights
  }

  /**
   * Simulates pressing all buttons in this set and returns the final light states.
   */
  private fun Set<Button>.simulateButtonPresses(): List<Boolean> {
    val lightStates = MutableList(indicatorDiagram.targetLights.size) { false }

    for (button in this) {
      for (lightIndex in button.affectedIndices) {
        lightStates[lightIndex] = !lightStates[lightIndex]
      }
    }

    return lightStates
  }

  companion object {
    // Matches format: [lights] (button1) (button2) ... {requirements}
    private val MACHINE_REGEX =
      Regex("""\[(?<lights>.*?)]\s*(?<buttons>(\([^)]*\)\s*)+)\s*\{(?<requirements>.*?)}""")

    fun parse(machineStr: String): Machine {
      val match = MACHINE_REGEX.matchEntire(machineStr)
        ?: error("Invalid machine format: $machineStr")

      val lightsGroup = match.groups["lights"]?.value
        ?: error("Missing lights group")
      val buttonsGroup = match.groups["buttons"]?.value
        ?: error("Missing buttons group")
      val requirementsGroup = match.groups["requirements"]?.value
        ?: error("Missing requirements group")

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

private val solution = object : Solution<Input, Output>(2025, "Day10") {
  override fun parse(input: ProblemInput): Input =
    input.linesAs(Machine::parse)

  override fun format(output: Output): String =
    output.toString()

  override fun part1(input: Input): Output =
    input.sumOf { it.findMinimumButtonPresses() }

  override fun part2(input: Input): Output {
    // Part 2 not yet implemented
    return 0L
  }
}

fun main() = solution.run()
