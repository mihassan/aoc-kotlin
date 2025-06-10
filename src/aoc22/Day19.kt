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
      (index + 1) * findMaxGeodes(blueprint, totalMinutes)
    }
  }

  override fun part2(input: Input): Output {
    val totalMinutes = 32
    return input.take(3).map { blueprint ->
      findMaxGeodes(blueprint, totalMinutes)
    }.reduce(Int::times)
  }

  private fun findMaxGeodes(blueprint: Blueprint, totalMinutes: Int): Int {
    val rules = blueprint.rules.associate { it.robot to it.cost }
    val maxCost = Bag.of(
      rules.values
        .flatMap { it.entries.toList() }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, amounts) -> amounts.max() })

    fun step(
      robots: Minerals,
      minerals: Minerals,
      minutesLeft: Int,
      skippedRobots: Set<Mineral> = emptySet(),
    ): Int {
      if (minutesLeft <= 0) {
        return minerals[Mineral.GEODE]
      }

      // Find candidates for robots to construct
      // 1. Must have enough minerals to construct.
      // 2. Must not be a skipped robot. This is an optimization where if we skip a robot, we do not
      //    need to construct the robot until we construct any other robot.
      // 3. Must be useful to construct. If we have enough robots to mine resources to construct any
      //    robot each minute, unless it is a geode robot, we do not need to construct more robots.
      val candidates = rules
        .filter { it.value isSubSetOf minerals }
        .filter { it.key !in skippedRobots }
        .filter { it.key == Mineral.GEODE || robots[it.key] < maxCost[it.key] }
        .keys

      // Produce minerals
      minerals += robots

      var maxGeodes = 0

      fun constructRobot(robot: Mineral): Int {
        // Construct robot
        minerals -= rules[robot]!!
        robots += robot

        step(robots, minerals, minutesLeft - 1)
          .takeIf { it > maxGeodes }
          ?.let { maxGeodes = it }

        // Backtrack to before construction
        minerals += rules[robot]!!
        robots -= robot

        return maxGeodes
      }

      // A series of heuristics.
      // 1. If we can construct a geode robot, do it.
      if (Mineral.GEODE in candidates) {
        maxGeodes = constructRobot(Mineral.GEODE)
      }
      // 2. If we have enough robots to construct any robot each minute,
      // we don't need to worry about creating more.
      else if (maxCost isSubSetOf robots) {
        maxGeodes = step(robots, minerals, minutesLeft - 1)
      }
      // 3. If all heuristics fail, we need to try all candidates.
      else {
        val newSkippedRobots = candidates + skippedRobots
        // 4. If we can construct all robots except geode, we must construct a robot.
        if (newSkippedRobots.size < 3) {
          maxGeodes = step(robots, minerals, minutesLeft - 1, newSkippedRobots)
        }
        candidates.forEach { constructRobot(it) }
      }

      // Backtrack to before production
      minerals -= robots

      return maxGeodes
    }

    return step(Bag.of(Mineral.ORE to 1), Bag.of(), totalMinutes)
  }
}

fun main() = solution.run()
