@file:Suppress("PackageDirectoryMismatch")

package aoc21.day12

import lib.Solution

private sealed interface Node {
  data object Start : Node

  data object End : Node

  data class Small(val name: String) : Node

  data class Big(val name: String) : Node

  companion object {
    fun parse(name: String): Node = when {
      name == "start" -> Start
      name == "end" -> End
      name.all { it.isLowerCase() } -> Small(name)
      name.all { it.isUpperCase() } -> Big(name)
      else -> throw IllegalArgumentException("Invalid node name: $name")
    }
  }
}

private data class Graph(val edges: Map<Node, Set<Node>>) {
  companion object {
    fun parse(graphStr: String): Graph = buildMap {
      graphStr.lines().forEach { line ->
        val (from, to) = line.split("-").map(Node.Companion::parse)
        computeIfAbsent(from) { mutableSetOf() }.add(to)
        computeIfAbsent(to) { mutableSetOf() }.add(from)
      }
    }.let(::Graph)
  }
}

private typealias Input = Graph

private typealias Output = Int

private val solution = object : Solution<Input, Output>(2021, "Day12") {
  override fun parse(input: String): Input = Graph.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = dfs(input) { visited ->
    visited.filter { it.key !is Node.Big }.values.all { it <= 1 }
  }

  override fun part2(input: Input): Output = dfs(input) { visited ->
    val startVisit = visited[Node.Start] ?: 0
    val smallVisits = visited.filter { it.key is Node.Small }.values

    startVisit <= 1 && smallVisits.all { it <= 2 } && smallVisits.count { it > 1 } <= 1
  }

  private fun dfs(graph: Graph, validPath: (Map<Node, Int>) -> Boolean): Int {
    val visited = mutableMapOf<Node, Int>()

    fun dfsVisit(node: Node): Int {
      if (!validPath(visited)) return 0
      if (node == Node.End) return 1

      visited[node] = (visited[node] ?: 0) + 1
      val pathCount = graph.edges[node]!!.sumOf { dfsVisit(it) }
      visited[node] = (visited[node] ?: 0) - 1

      return pathCount
    }

    return dfsVisit(Node.Start)
  }
}

fun main() = solution.run()
