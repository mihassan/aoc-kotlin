@file:Suppress("PackageDirectoryMismatch")

package aoc23.day12

import aoc23.day12.Condition.*
import lib.Solution
import lib.Strings.extractInts

enum class Condition(val symbol: Char) {
  OPERATIONAL('.'), DAMAGED('#'), UNKNOWN('?');

  override fun toString(): String = "$symbol"

  companion object {
    fun parse(symbol: Char): Condition = values().find { it.symbol == symbol }!!
  }
}

data class Conditions(val conditions: List<Condition>) {
  override fun toString(): String = conditions.joinToString("")

  fun noDamageAfter(idx: Int): Boolean = DAMAGED !in conditions.subList(idx, conditions.size)

  fun noDamageBetween(range: IntRange): Boolean =
    DAMAGED !in conditions.subList(range.first, range.last + 1)

  companion object {
    fun parse(conditionsStr: String): Conditions =
      Conditions(conditionsStr.map { Condition.parse(it) })
  }
}

data class ConditionRecord(val conditions: Conditions, val groups: List<Int>) {
  private data class DpKey(val conditionsIdx: Int, val groupsIdx: Int)

  fun unfold(times: Int): ConditionRecord {
    val conditionsWithoutPrefix = conditions.conditions.drop(1)
    val newConditions = Conditions(buildList<Condition> {
      addAll(conditions.conditions)
      repeat(times - 1) {
        add(UNKNOWN)
        addAll(conditionsWithoutPrefix)
      }
    })

    val newGroups = List(times) { groups }.flatten()

    return ConditionRecord(newConditions, newGroups)
  }

  fun arrangements(): Long {
    // Dynamic programming cache to avoid recomputing the same sub-problems.
    val dp: MutableMap<DpKey, Long> = mutableMapOf()

    fun solve(dpKey: DpKey): Long {
      // Return dp value if already computed.
      if (dpKey in dp) return dp[dpKey]!!

      // Base case: no more groups to match.
      if (dpKey.groupsIdx >= groups.size) {
        // If there are still damages in the remaining conditions, this arrangement is invalid.
        return if (conditions.noDamageAfter(dpKey.conditionsIdx)) 1 else 0
      }

      return findAllGroups(dpKey)
        .filter { conditions.noDamageBetween(dpKey.conditionsIdx..it) }
        .map { match ->
          DpKey(match + groups[dpKey.groupsIdx] + 1, dpKey.groupsIdx + 1)
        }
        .sumOf { solve(it) }.also {
          // Cache result.
          dp[dpKey] = it
        }
    }

    return solve(DpKey(0, 0))
  }

  private fun computeGroupRegex(groupsIdx: Int): Regex {
    val group = groups[groupsIdx]
    // Each part must start with an operational part.
    val operationalPart = "[${UNKNOWN.symbol}${OPERATIONAL.symbol}]"
    // Followed by a damaged part of length group.
    val damagedPart = "[${UNKNOWN.symbol}${DAMAGED.symbol}]".repeat(group)

    // The group regex is the operational part followed by the damaged part. We use a lookahead
    // trick to simulate overlapping matches.
    return Regex("""${operationalPart}(?=${damagedPart})""")
  }

  private fun findAllGroups(dpKey: DpKey): Sequence<Int> =
    computeGroupRegex(dpKey.groupsIdx)
      .findAll("$conditions", dpKey.conditionsIdx)
      .map { it.range.first }

  companion object {
    fun parse(lineStr: String): ConditionRecord {
      val (conditionsStr, groupsStr) = lineStr.split(" ")
      val conditions = Conditions.parse(".$conditionsStr")
      val groups = groupsStr.extractInts()

      return ConditionRecord(conditions, groups)
    }
  }
}

typealias Input = List<ConditionRecord>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2023, "Day12") {
  override fun parse(input: String): Input = input.lines().map { ConditionRecord.parse(it) }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.sumOf { it.arrangements() }

  override fun part2(input: Input): Output = part1(input.map { it.unfold(5) })
}

fun main() = solution.run()
