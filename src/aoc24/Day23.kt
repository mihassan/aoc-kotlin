@file:Suppress("PackageDirectoryMismatch")

package aoc24.day23

import aoc24.day23.Clique.Companion.cliqueOf
import kotlin.collections.sorted
import lib.Solution

@JvmInline
value class Node(val node: String) {
  fun isChiefNode() = node.first() == 't'
}

@JvmInline
value class Edge(val edge: Pair<Node, Node>) {
  val reversed: Edge get() = Edge(edge.second to edge.first)

  companion object {
    fun parse(edgeString: String): Edge {
      val (a, b) = edgeString.split("-")
      return Edge(Node(a) to Node(b))
    }
  }
}

@JvmInline
value class Clique(val nodes: Set<Node>) {
  fun add(node: Node): Clique = Clique(nodes + node)

  companion object {
    fun cliqueOf(node: Node): Clique = Clique(setOf(node))
  }
}

data class Graph(val nodes: Set<Node>, val edges: Map<Node, Set<Node>>) {
  fun findThreeNodes(): Set<Set<Node>> = nodes.flatMap { firstNode ->
    edges[firstNode]?.flatMap { secondNode ->
      edges[secondNode]?.mapNotNull { thirdNode ->
        setOf(firstNode, secondNode, thirdNode).takeIf { firstNode in edges[thirdNode]!! }
      } ?: emptySet()
    } ?: emptySet()
  }.toSet()

  fun findAllCliques(): Set<Clique>  {
    val visited = mutableSetOf<Clique>()
    val queue = ArrayDeque(nodes.map { cliqueOf(it) })

    while (queue.isNotEmpty()) {
      val clique = queue.removeFirst()
      nodes.forEach { node ->
        val newClique = addToClique(clique, node)
        if (newClique != null && newClique !in visited) {
          queue.add(newClique)
          visited.add(newClique)
        }
      }
    }

    return visited
  }

  private fun addToClique(clique: Clique, node: Node): Clique? {
    if (node in clique.nodes) return null
    val reachableNodes = edges[node]?.toSet() ?: return null
    if ((clique.nodes - reachableNodes).isNotEmpty()) return null
    return Clique(clique.nodes + node)
  }

  companion object {
    fun fromEdges(edges: List<Edge>): Graph {
      val nodes = edges.flatMap { listOf(it.edge.first, it.edge.second) }.toSet()
      val doubledEdges = edges.flatMap { listOf(it, it.reversed) }
      val edgesByNode = doubledEdges
        .groupBy({ it.edge.first }, { it.edge.second })
        .mapValues { it.value.toSet() }
      return Graph(nodes, edgesByNode)
    }
  }
}

typealias Input = Graph

typealias Output = String

private val solution = object : Solution<Input, Output>(2024, "Day23") {
  override fun parse(input: String): Input = input.lines().map(Edge::parse).let(Graph::fromEdges)

  override fun format(output: Output): String = output

  override fun part1(input: Input): Output =
    input.findThreeNodes().filter { it.any { it.isChiefNode() } }.let { return "${it.size}" }

  override fun part2(input: Input): Output {
    val maxClique = input.findAllCliques().maxBy { it.nodes.size }
    return maxClique.nodes.map { it.node }.sorted().joinToString(",")
  }
}

fun main() = solution.run()
