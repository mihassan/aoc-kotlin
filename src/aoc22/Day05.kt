@file:Suppress("PackageDirectoryMismatch")

package aoc22.day05

import lib.Collections.headTail
import lib.Solution
import lib.Strings.ints

typealias StackIndex = Int

data class Crate(private val char: Char) {
  override fun toString(): String = "$char"

  companion object {
    fun parse(char: Char): Crate? = Crate(char).takeIf { char.isUpperCase() }
  }
}

data class CrateStack(private val arrayDeque: ArrayDeque<Crate> = ArrayDeque()) {
  fun copy() = CrateStack(ArrayDeque(this.arrayDeque))

  fun push(crate: Crate) = arrayDeque.addLast(crate)
  fun pushN(crates: List<Crate>) = arrayDeque.addAll(crates)
  fun pop(): Crate = arrayDeque.removeLast()
  fun popN(n: Int): List<Crate> = arrayDeque.takeLast(n).also {
    repeat(it.size) { arrayDeque.removeLast() }
  }

  fun top(): Crate = arrayDeque.last()
}

data class Cargo(
  private val stackCount: Int,
  private val stacks: List<CrateStack> = List(stackCount) { CrateStack() },
) {
  fun copy() = Cargo(stackCount, stacks.map(CrateStack::copy))

  fun push(stackIndex: StackIndex, crate: Crate) = stacks[stackIndex].push(crate)
  fun pushN(stackIndex: StackIndex, crates: List<Crate>) = stacks[stackIndex].pushN(crates)
  fun pop(stackIndex: StackIndex): Crate = stacks[stackIndex].pop()
  fun popN(stackIndex: StackIndex, n: Int) = stacks[stackIndex].popN(n)

  fun topCrates(): List<Crate> = stacks.map(CrateStack::top)

  companion object {
    fun parse(stackCount: Int, stackLines: List<String>) =
      Cargo(stackCount).apply {
        stackLines.forEach { line ->
          line.forEachIndexed { idx, char ->
            Crate.parse(char)?.let { crate ->
              push(idx / 4, crate)
            }
          }
        }
      }
  }
}

data class Step(val quantity: Int, val from: StackIndex, val to: StackIndex) {
  companion object {
    fun parse(line: String): Step {
      val parts = line.split(" ")
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
    fun parse(procedureLines: List<String>) =
      Procedure(procedureLines.map { line -> Step.parse(line) })
  }
}

data class Input(val cargo: Cargo, val procedure: Procedure)

typealias Output = Cargo

private val solution = object : Solution<Input, Output>(2022, "Day05") {
  override fun parse(input: String): Input {
    val (cargoLines, procedureLines) = input.split("\n\n").map { it.lines() }
    val (stackIndexLine, stackLines) = cargoLines.reversed().headTail()
    val stackCount = checkNotNull(stackIndexLine).ints().max()

    return Input(Cargo.parse(stackCount, stackLines), Procedure.parse(procedureLines))
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
