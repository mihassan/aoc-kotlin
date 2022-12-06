@file:Suppress("PackageDirectoryMismatch")

package aoc22.day05

import lib.Solution
import lib.Strings.ints
import lib.Strings.words

typealias Crate = Char
typealias StackIndex = Int

data class CrateStack(private val stack: ArrayDeque<Crate> = ArrayDeque()) {
  fun copy(): CrateStack {
    val newStack = ArrayDeque<Crate>()
    newStack.addAll(stack)
    return CrateStack(newStack)
  }

  fun push(crate: Crate) = stack.addFirst(crate)
  fun pop(): Crate = stack.removeFirst()
  fun add(crate: Crate) = stack.addLast(crate)
  fun remove(): Crate = stack.removeFirst()
  fun top(): Crate = stack.first()
}

data class Cargo(
  private val stackCount: Int,
  private val stacks: List<CrateStack> = List(stackCount) { CrateStack() },
) {
  fun copy(): Cargo {
    val newStacks = stacks.map { it.copy() }
    return Cargo(stackCount, newStacks)
  }

  fun push(stackIndex: StackIndex, crate: Crate) {
    stacks[stackIndex].push(crate)
  }

  fun pop(stackIndex: StackIndex): Crate {
    return stacks[stackIndex].pop()
  }

  fun add(stackIndex: StackIndex, crate: Crate) {
    stacks[stackIndex].add(crate)
  }

  fun remove(stackIndex: StackIndex): Crate {
    return stacks[stackIndex].remove()
  }

  fun topCrates(): List<Crate> = stacks.map { it.top() }

  companion object {
    fun parseCargo(stackCount: Int, cargoLines: List<String>): Cargo {
      val cargo = Cargo(stackCount)

      fun getCrate(line: String, stackIndex: StackIndex): Crate? =
        line.getOrNull(4 * stackIndex + 1)?.takeIf { it.isUpperCase() }

      cargoLines.reversed().forEach { line ->
        repeat(stackCount) { stackIndex ->
          getCrate(line, stackIndex)?.let { crate ->
            cargo.push(stackIndex, crate)
          }
        }
      }

      return cargo
    }
  }
}

typealias Procedure = List<Step>

data class Step(val quantity: Int, val from: StackIndex, val to: StackIndex) {
  companion object {
    fun of(str: String): Step {
      val parts = str.words()
      val quantity = parts[1].toInt()
      val from = parts[3].toInt() - 1
      val to = parts[5].toInt() - 1
      return Step(quantity, from, to)
    }
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

    return Input(Cargo.parseCargo(stackCount, cargoLines), procedureLines.map { Step.of(it) })
  }

  override fun format(output: Output): String = output.topCrates().joinToString("")

  override fun part1(input: Input): Output {
    val cargo: Cargo = input.cargo.copy()

    input.procedure.forEach { (quantity, from, to) ->
      repeat(quantity) {
        val crate = cargo.pop(from)
        cargo.push(to, crate)
      }
    }
    return cargo
  }

  override fun part2(input: Input): Output {
    val cargo: Cargo = input.cargo.copy()

    input.procedure.forEach { (quantity, from, to) ->
      val crates = List(quantity) { cargo.pop(from) }
      crates.reversed().forEach { crate -> cargo.push(to, crate) }
    }
    return cargo
  }
}

fun main() = solution.run()
