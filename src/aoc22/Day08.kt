@file:Suppress("PackageDirectoryMismatch")

package aoc22.day06

import kotlin.math.max
import lib.Collections.flipHorizontally
import lib.Collections.suffixes
import lib.Collections.transposed
import lib.Collections.zip2
import lib.Grid
import lib.Solution


typealias Input = Grid<Int>
typealias Output = Int

private val solution = object : Solution<Input, Output>(2022, "Day08") {
  override fun parse(input: String): Input =
    input.lines().map { row ->
      row.toCharArray().map { col ->
        col.digitToInt()
      }
    }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    return with(input) {
      listOf(
        visibleFromLeft(),
        visibleFromRight(),
        visibleFromTop(),
        visibleFromBottom()
      )
    }.reduce { x, y ->
      x.zip2(y, Boolean::or)
    }.flatten().count { it }
  }

  override fun part2(input: Input): Output {
    return with(input) {
      listOf(
        visibleTreesOnRight(),
        visibleTreesOnLeft(),
        visibleTreesOnTop(),
        visibleTreesOnBottom()
      )
    }.reduce { x, y ->
      x.zip2(y, Int::times)
    }.flatten().max()
  }

  private fun Input.visibleFromLeft(): Grid<Boolean> =
    map { row ->
      row
        .runningFold(Int.MIN_VALUE, ::max)
        .zip(row) { maxHeight, height -> maxHeight < height }
    }

  private fun Input.visibleFromRight(): Grid<Boolean> =
    flipHorizontally().visibleFromLeft().flipHorizontally()

  private fun Input.visibleFromTop(): Grid<Boolean> =
    transposed().visibleFromLeft().transposed()

  private fun Input.visibleFromBottom(): Grid<Boolean> =
    transposed().visibleFromRight().transposed()

  private fun Input.visibleTreesOnRight(): Grid<Int> =
    map { row ->
      row.suffixes().map { suffix ->
        val targetHeight = suffix.first()
        val allRightTrees = suffix.withIndex().drop(1)
        val blockingTree = allRightTrees.find { it.value >= targetHeight }
        blockingTree?.index ?: (suffix.size - 1)
      }
    }

  private fun Input.visibleTreesOnLeft(): Grid<Int> =
    flipHorizontally().visibleTreesOnRight().flipHorizontally()

  private fun Input.visibleTreesOnTop(): Grid<Int> =
    transposed().visibleTreesOnLeft().transposed()

  private fun Input.visibleTreesOnBottom(): Grid<Int> =
    transposed().visibleTreesOnRight().transposed()
}

fun main() = solution.run()
