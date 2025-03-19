@file:Suppress("PackageDirectoryMismatch")

package aoc24.day23

import lib.Solution

data class Node(val node: String) : Comparable<Node> {
  fun isChiefNode(): Boolean = node.startsWith("t")
  override fun compareTo(other: Node): Int = node.compareTo(other.node)
}

data class Graph(val nodes: Set<Node>, val edges: Map<Node, Set<Node>>) {
  companion object {
    fun parse(graphStr: String): Graph {
      val nodes = mutableSetOf<Node>()
      val edges = mutableMapOf<Node, MutableSet<Node>>()

      graphStr.lines().forEach { line ->
        val (a, b) = line.split("-")
        val nodeA = Node(a)
        val nodeB = Node(b)

        nodes += nodeA
        nodes += nodeB

        edges.getOrPut(nodeA) { mutableSetOf() } += nodeB
        edges.getOrPut(nodeB) { mutableSetOf() } += nodeA
      }

      return Graph(nodes, edges)
    }
  }
}

typealias Input = Graph

typealias Output = String

private val solution = object : Solution<Input, Output>(2024, "Day23") {
  override fun parse(input: String): Input = Graph.parse(input)

  override fun format(output: Output): String = output

  override fun part1(input: Input): Output {
    var result = 0

    input.nodes.forEach { node1 ->

      input.edges[node1]!!.forEach { node2 ->
        // Avoid duplicates by only considering nodes in ascending order
        if (node1 > node2) return@forEach

        input.edges[node2]!!.forEach { node3 ->
          // Avoid duplicates by only considering nodes in ascending order
          if (node2 > node3) return@forEach

          // If the edge is missing, the nodes are not connected
          if (node3 !in input.edges[node1]!!) return@forEach

          // If any of the nodes is a chief node, increment the result
          if (listOf(node1, node2, node3).any { it.isChiefNode() }) {
            result++
          }
        }
      }
    }

    return "$result"
  }

  override fun part2(input: Input): Output {
    val clique = ArrayDeque<Node>()
    var maxClique = emptyList<Node>()

    fun dfs(node: Node) {
      if (clique.size > maxClique.size) {
        maxClique = clique.toList()
      }

      input.edges[node]?.forEach { newNode ->
        // Skip if the node is already in the clique
        if (newNode in clique) return@forEach

        // Avoid checking the same clique multiple times by only considering nodes in ascending order
        if (clique.any { it > newNode }) return@forEach

        // Skip if the new node is not connected to all nodes in the clique
        val newEdges = input.edges[newNode]!!
        if (clique.any { it !in newEdges }) return@forEach

        // Add the new node to the clique and continue the search
        clique.addLast(newNode)
        dfs(newNode)
        clique.removeLast()
      }
    }

    // Start the search from each node in the graph
    input.nodes.forEach {
      clique.add(it)
      dfs(it)
      clique.clear()
    }

    return maxClique.joinToString(",") { it.node }
  }
}

fun main() = solution.run()
