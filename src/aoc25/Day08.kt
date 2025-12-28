@file:Suppress("PackageDirectoryMismatch")

package aoc25.day08

import kotlin.math.sqrt
import lib.Solution

private data class Point3D(val x: Long, val y: Long, val z: Long) {
  infix fun distanceTo(other: Point3D): Double {
    val dx = (other.x - x).toDouble()
    val dy = (other.y - y).toDouble()
    val dz = (other.z - z).toDouble()
    return sqrt(dx * dx + dy * dy + dz * dz)
  }

  companion object {
    fun parse(input: String): Point3D {
      val (x, y, z) = input.split(",").map(String::toLong)
      return Point3D(x, y, z)
    }
  }
}

private data class Distance(val from: Int, val to: Int, val distance: Double)

private class UnionFind(val size: Int) {
  private val parent = IntArray(size) { it }

  fun find(node: Int): Int {
    if (parent[node] != node) {
      parent[node] = find(parent[node])
    }
    return parent[node]
  }

  fun union(node1: Int, node2: Int): Boolean {
    val root1 = find(node1)
    val root2 = find(node2)
    return if (root1 != root2) {
      parent[root2] = root1
      true
    } else {
      false
    }
  }

  fun groupSizes(): Map<Int, Int> = (0 until size).groupingBy { find(it) }.eachCount()
}

private typealias Input = List<Point3D>
private typealias Output = Long

private val solution = object : Solution<Input, Output>(2025, "Day08") {
  override fun parse(input: String): Input = input.lines().map(Point3D::parse)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    val circuits = UnionFind(input.size)
    val distances = computeAllDistances(input)

    joinNClosestJunctions(circuits, distances, n = 1000)

    return circuits.groupSizes().values.sortedDescending().take(3)
      .fold(1L) { acc, size -> acc * size }
  }

  override fun part2(input: Input): Output {
    val circuits = UnionFind(input.size)
    val distances = computeAllDistances(input).sortedBy { it.distance }
    var connectionCount = 0

    for (distance in distances) {
      if (circuits.union(distance.from, distance.to)) {
        connectionCount++
      }
      // A spanning tree has exactly (n - 1) edges
      if (connectionCount == input.size - 1) {
        return input[distance.from].x * input[distance.to].x
      }
    }

    error("Could not connect all junctions")
  }

  private fun computeAllDistances(points: List<Point3D>): List<Distance> =
    points.indices.flatMap { i ->
      (i + 1 until points.size).map { j ->
        Distance(i, j, points[i] distanceTo points[j])
      }
    }

  private fun joinNClosestJunctions(circuits: UnionFind, distances: List<Distance>, n: Int) {
    distances.sortedBy { it.distance }.take(n).forEach { circuits.union(it.from, it.to) }
  }
}

fun main() = solution.run()
