@file:Suppress("PackageDirectoryMismatch")

package aoc22.day19

import lib.Bag
import lib.Solution
import lib.Strings.words

enum class Mineral {
  ORE, CLAY, OBSIDIAN, GEODE;

  companion object {
    fun parse(mineral: String): Mineral = Mineral.values().find { it.name.lowercase() == mineral }
      ?: error("Bad input for Mineral: $mineral")
  }
}

typealias Minerals = Bag<Mineral>

data class Rule(val robot: Mineral, val cost: Minerals) {
  companion object {
    fun parse(rule: String): Rule {
      val (robotPart, costPart) = rule.removePrefix("Each ").removeSuffix(".")
        .split(" robot costs ")

      val robot = Mineral.parse(robotPart)

      val minerals = Bag.of(costPart.split(" and ").associate {
        val (amount, mineral) = it.words()
        Mineral.parse(mineral) to amount.toInt()
      })

      return Rule(robot, minerals)
    }
  }
}

data class Blueprint(val rules: List<Rule>) {
  companion object {
    fun parse(blueprint: String): Blueprint {
      val rules = blueprint.substringAfter(": ").split(". ").map { Rule.parse(it) }
      return Blueprint(rules)
    }
  }
}

typealias Input = List<Blueprint>

typealias Output = Int

private val solution = object : Solution<Input, Output>(2022, "Day19") {
  override fun parse(input: String): Input {
    return input.lines().map { Blueprint.parse(it) }
  }

  override fun format(output: Output): String {
    return "$output"
  }

  override fun part1(input: Input): Output {
    val totalMinutes = 24
    return input.withIndex().sumOf { (index, blueprint) ->
      (index + 1) * findMaxGeodes(
        blueprint, Bag.of(Mineral.ORE), Bag.of(), totalMinutes
      )
    }
  }

  override fun part2(input: Input): Output {
    TODO("Not yet implemented")
  }

  private fun findMaxGeodes(
    blueprint: Blueprint,
    robots: Minerals,
    minerals: Minerals,
    minutesLeft: Int,
    skippedRobots: Set<Mineral> = emptySet(),
  ): Int {
    if (minutesLeft <= 0) {
      return minerals[Mineral.GEODE]
    }

    val robotCandidates: Map<Mineral, Minerals> =
      blueprint.rules.filter { it.cost.isSubSetOf(minerals) }.associate { it.robot to it.cost }

    minerals += robots

    var maxGeodes = findMaxGeodes(
      blueprint,
      robots,
      minerals,
      minutesLeft - 1,
      skippedRobots + robotCandidates.keys
    )

    fun maybeConstructRobot(robot: Mineral) {
      if (robot in skippedRobots || robot !in robotCandidates) return

      minerals -= robotCandidates[robot]!!
      robots += robot

      findMaxGeodes(blueprint, robots, minerals, minutesLeft - 1)
        .takeIf { it > maxGeodes }
        ?.let {
          maxGeodes = it
        }

      // Backtrack to before construction
      minerals += robotCandidates[robot]!!
      robots -= robot
    }

    robotCandidates.forEach { maybeConstructRobot(it.key) }

    // Backtrack to before production
    minerals -= robots

    return maxGeodes
  }
}

fun main() = solution.run()
