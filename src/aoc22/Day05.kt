@file:Suppress("PackageDirectoryMismatch")

package aoc22.day05

import java.util.Stack
import lib.Solution
import lib.Strings.ints
import lib.Strings.words

typealias Crate = Char
typealias StackIndex = Int

typealias CrateStack = Stack<Crate>

@Suppress("UNCHECKED_CAST")
fun CrateStack.copy() = this.clone() as CrateStack

data class Cargo(
  private val stackCount: Int,
  private val stacks: List<CrateStack> = List(stackCount) { CrateStack() },
) {
  fun copy() = Cargo(stackCount, stacks.map { it.copy() })

  fun push(stackIndex: StackIndex, crate: Crate) {
    stacks[stackIndex].push(crate)
  }

  fun pushN(stackIndex: StackIndex, crates: List<Crate>) {
    crates.forEach { crate ->
      stacks[stackIndex].push(crate)
    }
  }

  fun pop(stackIndex: StackIndex): Crate {
    return stacks[stackIndex].pop()
  }

  fun popN(stackIndex: StackIndex, n: Int): List<Crate> {
    val stack = stacks[stackIndex]
    val crates = stack.takeLast(n)
    stack.setSize(stack.size - n)
    return crates
  }

  fun topCrates(): List<Crate> = stacks.map { it.peek() }

  companion object {
    fun parse(stackCount: Int, cargoLines: List<String>): Cargo {
      fun getCrate(line: String, stackIndex: StackIndex): Crate? =
        line.getOrNull(4 * stackIndex + 1)?.takeIf { it.isUpperCase() }

      fun getStack(stackIndex: StackIndex): List<Crate> =
        cargoLines.mapNotNull { line ->
          getCrate(line, stackIndex)
        }

      val stacks = List(stackCount) { stackIndex ->
        CrateStack().apply { addAll(getStack(stackIndex).reversed()) }
      }

      return Cargo(stackCount, stacks)
    }
  }
}

data class Step(val quantity: Int, val from: StackIndex, val to: StackIndex) {
  companion object {
    fun parse(line: String): Step {
      val parts = line.words()
      val quantity = parts[1].toInt()
      val from = parts[3].toInt() - 1
      val to = parts[5].toInt() - 1
      return Step(quantity, from, to)
    }
  }
}

data class Procedure(val steps: List<Step>) {
  fun run(block: (step: Step) -> Unit) = steps.forEach(block)

  companion object {
    fun parse(procedureLines: List<String>): Procedure =
      Procedure(procedureLines.map { line -> Step.parse(line) })
  }
}

data class Input(val cargo: Cargo, val procedure: Procedure)

typealias Output = Cargo

private val solution = object : Solution<Input, Output>(2022, "Day05") {
  override fun parse(input: String): Input {
    val lines = input.lines()
    val inputSplitPoint = lines.indexOf("")

    val cargoLines = lines.take(inputSplitPoint - 1)
    val procedureLines = lines.drop(inputSplitPoint + 1)

    val stackCount = lines[inputSplitPoint - 1].ints().count()

    return Input(Cargo.parse(stackCount, cargoLines), Procedure.parse(procedureLines))
  }

  override fun format(output: Output): String = output.topCrates().joinToString("")

  override fun part1(input: Input): Output {
    val cargo: Cargo = input.cargo.copy()

    input.procedure.run { (quantity, from, to) ->
      repeat(quantity) {
        val crate = cargo.pop(from)
        cargo.push(to, crate)
      }
    }
    return cargo
  }

  override fun part2(input: Input): Output {
    val cargo: Cargo = input.cargo.copy()

    input.procedure.run { (quantity, from, to) ->
      val crates = cargo.popN(from, quantity)
      cargo.pushN(to, crates)
    }
    return cargo
  }
}

fun main() = solution.run()
