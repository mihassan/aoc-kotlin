@file:Suppress("PackageDirectoryMismatch")

package aoc22.day19

import lib.Bag
import lib.Solution
import lib.Strings.words

enum class Mineral {
  ORE, CLAY, OBSIDIAN, GEODE;

  companion object {
    fun parse(mineral: String): Mineral = when (mineral) {
      "ore" -> ORE
      "clay" -> CLAY
      "obsidian" -> OBSIDIAN
      "geode" -> GEODE
      else -> error("Bad input for Mineral: $mineral")
    }
  }
}

data class MineralCollection(val minerals: Bag<Mineral>) {
  operator fun plus(mineral: Mineral) = MineralCollection(minerals + mineral)

  operator fun plus(other: MineralCollection) = MineralCollection(minerals + other.minerals)

  operator fun minus(mineral: Mineral) = MineralCollection(minerals - mineral)

  operator fun minus(other: MineralCollection) = MineralCollection(minerals - other.minerals)

  operator fun times(multiplicand: Int) = MineralCollection(minerals * multiplicand)

  companion object {
    fun parse(minerals: String): MineralCollection {
      val mineralsMap = minerals.split(" and ").associate {
        val (amount, mineral) = it.words()
        Mineral.parse(mineral) to amount.toInt()
      }
      return MineralCollection(Bag.of(mineralsMap))
    }

    fun of(vararg minerals: Mineral) = MineralCollection(Bag.of(*minerals))
  }
}


data class Robot(val canCollect: Mineral) {
  fun produceMinerals() = MineralCollection.of(canCollect)

  companion object {
    fun parse(robot: String): Robot = Robot(Mineral.parse(robot))
  }
}

data class RobotCollection(val robots: Bag<Robot>) {
  operator fun plus(robot: Robot) = RobotCollection(robots + robot)

  operator fun plus(other: RobotCollection) = RobotCollection(robots + other.robots)

  operator fun minus(robot: Robot) = RobotCollection(robots - robot)

  operator fun minus(other: RobotCollection) = RobotCollection(robots - other.robots)

  fun produceMinerals() =
    robots.entries.entries.fold(MineralCollection.of()) { acc, (robot, robotCount) ->
      acc + robot.produceMinerals() * robotCount
    }

  companion object {
    fun of(vararg robots: Robot) = RobotCollection(Bag.of(*robots))
  }
}

data class Rule(val robot: Robot, val cost: MineralCollection) {
  companion object {
    fun parse(rule: String): Rule {
      val (robot, mineralAmount) = rule.removePrefix("Each ").removeSuffix(".")
        .split(" robot costs ")
      return Rule(Robot.parse(robot), MineralCollection.parse(mineralAmount))
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
    return input.mapIndexed { index, blueprint ->
      findMaxGeodes(
        blueprint,
        RobotCollection.of(Robot(Mineral.ORE)),
        MineralCollection.of(),
        24
      ) * (index + 1)
    }.sum()
  }

  override fun part2(input: Input): Output {
    TODO("Not yet implemented")
  }

  private fun findMaxGeodes(
    blueprint: Blueprint,
    robots: RobotCollection,
    minerals: MineralCollection,
    minutesLeft: Int,
    skippedRobots: Set<Robot> = emptySet(),
  ): Int {
    if (minutesLeft <= 0) {
      return minerals.minerals[Mineral.GEODE]
    }

    val mineralsAfterProduction = minerals + robots.produceMinerals()

    val robotCandidates: Map<Robot, MineralCollection> = blueprint.rules
      .filter { it.cost.minerals.isSubSetOf(minerals.minerals) }
      .associate { it.robot to it.cost }

    var maxGeodes = findMaxGeodes(
      blueprint,
      robots,
      mineralsAfterProduction,
      minutesLeft - 1,
      skippedRobots + robotCandidates.keys
    )

    fun maybeConstructRobot(robot: Robot) {
      if (robot in skippedRobots || robot !in robotCandidates)
        return
      val mineralsAfterConstruction = mineralsAfterProduction - robotCandidates[robot]!!
      val robotsAfterConstruction = robots + robot
      val maxGeodesAfterConstruction =
        findMaxGeodes(
          blueprint,
          robotsAfterConstruction,
          mineralsAfterConstruction,
          minutesLeft - 1
        )
      maxGeodes = maxOf(maxGeodes, maxGeodesAfterConstruction)
    }

    robotCandidates.forEach { maybeConstructRobot(it.key) }

    return maxGeodes
  }
}

fun main() = solution.run()
