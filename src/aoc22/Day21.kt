@file:Suppress("PackageDirectoryMismatch")

package aoc22.day21

import kotlin.math.sign
import lib.Solution
import lib.Strings.isLong

enum class Operation(val symbol: String, val op: (Long, Long) -> Long) {
  ADD("+", Long::plus),
  SUBTRACT("-", Long::minus),
  MULTIPLY("*", Long::times),
  DIVIDE("/", Long::div),

  // Using minus here to measure how far off we are from the equality.
  EQUALS("=", Long::minus);

  fun apply(a: Long, b: Long): Long = op(a, b)

  companion object {
    fun parse(symbol: String): Operation = values().first { it.symbol == symbol }
  }
}

sealed interface Job {
  data class FixedNumber(val num: Long) : Job
  data class RunOperation(val op: Operation, val op1: String, val op2: String) : Job

  companion object {
    fun parse(jobStr: String): Job =
      if (jobStr.isLong()) {
        FixedNumber(jobStr.toLong())
      } else {
        val (op1, op, op2) = jobStr.split(" ")
        RunOperation(Operation.parse(op), op1, op2)
      }
  }
}

data class Monkey(val name: String, val job: Job) {
  companion object {
    fun parse(monkeyStr: String): Monkey {
      val (name, jobPart) = monkeyStr.split(": ")
      return Monkey(name, Job.parse(jobPart))
    }
  }
}

typealias Input = List<Monkey>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2022, "Day21") {
  override fun parse(input: String): Input {
    return input.lines().map { Monkey.parse(it) }
  }

  override fun format(output: Output): String {
    return "$output"
  }

  override fun part1(input: Input): Output {
    val monkeyMap = input.associateBy { it.name }
    val cachedResults = mutableMapOf<String, Long>()

    fun calcResult(monkeyName: String): Long {
      return cachedResults[monkeyName] ?: run {
        val (name, job) = monkeyMap[monkeyName] ?: error("Unknown monkey: $monkeyName")
        when (job) {
          is Job.FixedNumber -> job.num
          is Job.RunOperation -> {
            job.op.apply(calcResult(job.op1), calcResult(job.op2))
          }
        }.also {
          cachedResults[name] = it
        }
      }
    }

    return calcResult("root")
  }

  override fun part2(input: Input): Output {
    val monkeys = input.toMutableList()

    // Update root job to be an `EQUALS` operation.
    val rootIdx = monkeys.indexOfFirst { it.name == "root" }
    val root = monkeys[rootIdx]
    val rootJob = root.job as? Job.RunOperation ?: error("Root job should not be a fixed number.")
    monkeys[rootIdx] = root.copy(job = rootJob.copy(op = Operation.EQUALS))

    // Check the root value with the humn number. If the result is 0, then we have the right number.
    fun check(humnNum: Long): Long {
      val humnIdx = monkeys.indexOfFirst { it.name == "humn" }
      val humn = monkeys[humnIdx]
      monkeys[humnIdx] = humn.copy(job = Job.FixedNumber(humnNum))
      return part1(monkeys)
    }

    var queryRange = -1L..1L
    // Expansion phase. Keep increasing the range until we have different signs for the result.
    while (check(queryRange.first).sign == check(queryRange.last).sign) {
      queryRange = queryRange.first * 2..queryRange.last * 2
    }

    // Binary search phase. Keep shrinking the range until we have the right number.
    while (queryRange.first != queryRange.last) {
      val lowValue = check(queryRange.first)
      val highValue = check(queryRange.last)

      val mid = (queryRange.first + queryRange.last) / 2
      val midValue = check(mid)

      if (midValue == 0L) {
        return mid
      }

      queryRange = if (lowValue.sign != midValue.sign) {
        queryRange.first..mid
      } else if (highValue.sign != midValue.sign) {
        mid..queryRange.last
      } else {
        error("Invalid state. The result should have different signs in both ends of the range")
      }
    }
    error("Could not find the solution.")
  }
}

fun main() = solution.run()
