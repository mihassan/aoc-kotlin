@file:Suppress("PackageDirectoryMismatch")

package aoc23.day12

import lib.Collections.headTail
import lib.Solution
import lib.Strings.extractInts

data class ConditionRecord(val conditions: String, val groups: List<Int>) {
  fun unfold(times: Int): ConditionRecord {
    val newConditions = List(times) { conditions }.joinToString("?")
    val newGroups = List(times) { groups }.flatten()
    return ConditionRecord(newConditions, newGroups)
  }

  companion object {
    fun parse(lineStr: String): ConditionRecord {
      val (conditions, groups) = lineStr.split(" ")
      return ConditionRecord(conditions, groups.extractInts())
    }
  }
}

typealias Input = List<ConditionRecord>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2023, "Day12") {
  override fun parse(input: String): Input = input.lines().map { ConditionRecord.parse(it) }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.sumOf { arrangements(it) }

  override fun part2(input: Input): Output = part1(input.map { it.unfold(5) })

  fun arrangements(conditionRecord: ConditionRecord): Long {
    // Dynamic programming cache to avoid recomputing the same sub-problems.
    val dp: MutableMap<ConditionRecord, Long> = mutableMapOf()

    fun solve(conditionRecord: ConditionRecord): Long {
      // Return dp value if already computed.
      if (conditionRecord in dp) return dp[conditionRecord]!!

      val (group, nextGroups) = conditionRecord.groups.headTail()

      // Base case: no more groups to match.
      if (group == null) {
        // If there are still damages in the remaining conditions, this arrangement is invalid.
        return if (conditionRecord.conditions.any { it == '#' }) 0 else 1
      }

      // The following regex matches a group of `group` consecutive damaged characters (either `#`
      // or `?`). We use a lookahead to simulate overlapping matches. The match must not be followed
      // by a `#` character, which would make the arrangement invalid.
      return Regex("""[^.](?=[^.]{${group - 1}}([^#]|$))""")
        .findAll(conditionRecord.conditions)
        .map { it.range.first }
        // Filter out matches that are preceded by a `#` character.
        .filter { '#' !in conditionRecord.conditions.take(it) }
        .sumOf {
          // Drop `group` characters from the conditions and one extra character to account for the
          // fact that two groups cannot be adjacent.
          val nextConditions = conditionRecord.conditions.drop(it + group + 1)
          val nextConditionRecord = ConditionRecord(nextConditions, nextGroups)
          // Recursively solve the sub-problem.
          solve(nextConditionRecord)
        }.also {
          // Cache the result.
          dp[conditionRecord] = it
        }
    }

    return solve(conditionRecord)
  }
}

fun main() = solution.run()
