@file:Suppress("PackageDirectoryMismatch")

package aoc22.day05_simple

import lib.Collections.headTail
import lib.ProblemInput
import lib.Solution
import lib.Strings.ints
import lib.Strings.words

private data class Step(val quantity: Int, val from: Int, val to: Int)

private data class Input(val cargo: List<ArrayDeque<Char>>, val procedure: List<Step>)

private typealias Output = List<ArrayDeque<Char>>

private val solution = object : Solution<Input, Output>(2022, "Day05") {
  override fun parse(input: ProblemInput): Input {
    val (cargoSection, procedureSection) = input.sections()
    val (stackIndexLine, stackLines) = cargoSection.lines().reversed().headTail()
    val stackCount = checkNotNull(stackIndexLine).ints().max()

    val steps = procedureSection.lines().map { line ->
      val (quantity, from, to) = line.words().mapNotNull(String::toIntOrNull)
      Step(quantity, from - 1, to - 1)
    }

    val cargo = List(stackCount) { ArrayDeque<Char>() }
    stackLines.forEach { line ->
      line.chunked(4).forEachIndexed { idx, ch ->
        ch[1].takeIf { it.isUpperCase() }?.let {
          cargo[idx].addLast(it)
        }
      }
    }

    return Input(cargo, steps)
  }

  override fun format(output: Output): String = output.joinToString("") { "${it.last()}" }

  override fun part1(input: Input): Output {
    val cargo = input.cargo.map { ArrayDeque(it) }

    input.procedure.forEach { (quantity, from, to) ->
      repeat(quantity) {
        val crate = cargo[from].removeLast()
        cargo[to].addLast(crate)
      }
    }

    return cargo
  }

  override fun part2(input: Input): Output {
    val cargo = input.cargo.map { ArrayDeque(it) }

    input.procedure.forEach { (quantity, from, to) ->
      val crates = cargo[from].takeLast(quantity)
      repeat(quantity) { cargo[from].removeLast() }
      cargo[to].addAll(crates)
    }

    return cargo
  }
}

fun main() = solution.run()
