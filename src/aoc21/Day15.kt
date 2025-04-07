@file:Suppress("PackageDirectoryMismatch")

package aoc21.day15

import java.util.PriorityQueue
import kotlin.Long
import kotlin.collections.isNotEmpty
import lib.Adjacency
import lib.Grid
import lib.Point
import lib.Solution

private data class PointWithRisk(val point: Point, val risk: Long) : Comparable<PointWithRisk> {
  override fun compareTo(other: PointWithRisk): Int = risk.compareTo(other.risk)
}

private data class Cave(val risks: Grid<Int>) {
  private val start: Point = Point(0, 0)
  private val end: Point = Point(risks.width - 1, risks.height - 1)
  private val totalRisksFromStart = mutableMapOf<Point, Long>()

  fun totalRiskFromStartToEnd(): Long {
    return totalRisksFromStart[end] ?: throw IllegalStateException("No path found to end")
  }

  fun calculateLowestRiskPaths() {
    // Dijkstra's algorithm to find the lowest risk path from start to end
    val visited = mutableSetOf<Point>()

    val queue = PriorityQueue<PointWithRisk>()
    queue.add(PointWithRisk(start, 0L))

    totalRisksFromStart.clear()
    totalRisksFromStart[start] = 0L

    while (queue.isNotEmpty()) {
      val (point, risk) = queue.remove()

      if (point in visited) continue
      visited.add(point)

      risks.adjacents(point, Adjacency.ORTHOGONAL).forEach { neighbor ->
        val newRisk = risk + risks[neighbor]!!
        if (newRisk < totalRisksFromStart.getOrDefault(neighbor, Long.MAX_VALUE)) {
          totalRisksFromStart[neighbor] = newRisk
          queue.add(PointWithRisk(neighbor, newRisk))
        }
      }
    }
  }

  fun duplicate(copies: Int): Cave {
    val newRisks: List<MutableList<Int>> =
      List(risks.height * copies) { y ->
        MutableList(risks.width * copies) { x ->
          val oldX = x % risks.width
          val oldY = y % risks.height
          val xCopy = x / risks.width
          val yCopy = y / risks.height
          (risks[Point(oldX, oldY)]!! + xCopy + yCopy + 8) % 9 + 1
        }
      }
    return Cave(Grid(newRisks))
  }

  companion object {
    fun parse(caveStr: String): Cave = Cave(Grid.parse(caveStr).map { it.digitToInt() })
  }
}

private typealias Input = Cave
private typealias Output = Long

private val solution = object : Solution<Input, Output>(2021, "Day15") {
  override fun parse(input: String): Input = Cave.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = with(input) {
    calculateLowestRiskPaths()
    totalRiskFromStartToEnd()
  }

  override fun part2(input: Input): Output = part1(input.duplicate(5))
}

fun main() = solution.run()
