@file:Suppress("PackageDirectoryMismatch")

package aoc24.day16

import aoc24.day16.Tile.Companion.isNonBlocking
import java.util.PriorityQueue
import lib.Direction
import lib.Grid
import lib.Point
import lib.Solution

enum class Tile(val char: Char) {
  EMPTY('.'), WALL('#'), START('S'), END('E');

  companion object {
    fun parse(tileChar: Char): Tile =
      entries.firstOrNull { it.char == tileChar } ?: error("Invalid tileChar: $tileChar")

    fun Tile?.isNonBlocking(): Boolean = this in setOf(EMPTY, START, END)
  }
}

data class Maze(val grid: Grid<Tile>) {
  val start: Point by lazy {
    grid.indexOfOrNull { it == Tile.START } ?: error("No start found")
  }
  val end: Point by lazy {
    grid.indexOfOrNull { it == Tile.END } ?: error("No end found")
  }

  fun neighborsOfWithCost(pose: Pose): List<Pair<Pose, Int>> =
    pose.neighborsWithCost().filter { grid[it.first.point].isNonBlocking() }

  companion object {
    fun parse(mazeStr: String): Maze = Maze(Grid.parse(mazeStr).map { Tile.parse(it) })
  }
}

data class Pose(val point: Point, val direction: Direction) {
  fun neighborsWithCost(): List<Pair<Pose, Int>> = listOf(
    step() to 1, turnLeft() to 1000, turnRight() to 1000
  )

  fun reverseNeighborsWithCost(): List<Pair<Pose, Int>> = listOf(
    stepBack() to 1, turnLeft() to 1000, turnRight() to 1000
  )

  private fun step(): Pose = copy(point = point.move(direction))
  private fun stepBack(): Pose = copy(point = point.move(direction.turnAround()))
  private fun turnLeft(): Pose = copy(direction = direction.turnLeft())
  private fun turnRight(): Pose = copy(direction = direction.turnRight())
}

data class PoseWithCost(val pose: Pose, val cost: Int)

typealias Input = Maze

typealias Output = Int

private val solution = object : Solution<Input, Output>(2024, "Day16") {
  override fun parse(input: String): Input = Maze.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    // Dijkstra's algorithm to find the shortest distance from the start point facing EAST/RIGHT.
    val distances = input.calculateShortestDistance(Pose(input.start, Direction.RIGHT))

    // Find the shortest distance to the end point facing any direction.
    return Direction.entries.minOfOrNull { distances[Pose(input.end, it)] ?: Int.MAX_VALUE }
      ?: error("No path found")
  }

  override fun part2(input: Input): Output {
    // Dijkstra's algorithm to find the shortest distance from the start point facing EAST/RIGHT.
    val distances = input.calculateShortestDistance(Pose(input.start, Direction.RIGHT))

    // Traverse back via the best paths to find the number of unique points visited.
    val endPose = distances.filterKeys { it.point == input.end }.minBy { it.value }.key
    val visited = mutableSetOf<Pose>()
    traverseBackViaBestPaths(distances, visited, endPose)

    // Return the number of points visited. All posses at a single point are considered as one.
    return visited.map { it.point }.toSet().size
  }

  // Dijkstra's algorithm to find the shortest distance from the start point.
  private fun Maze.calculateShortestDistance(start: Pose): MutableMap<Pose, Int> {
    val distances = mutableMapOf(start to 0)
    val queue = PriorityQueue<PoseWithCost> { a, b -> a.cost.compareTo(b.cost) }
    queue.add(PoseWithCost(start, 0))

    while (queue.isNotEmpty()) {
      val (currentPose, currentCost) = queue.poll()

      neighborsOfWithCost(currentPose).forEach { (neighbor, edgeCost) ->
        val newCost = currentCost + edgeCost
        if (newCost < distances.getOrDefault(neighbor, Int.MAX_VALUE)) {
          distances[neighbor] = newCost
          queue.add(PoseWithCost(neighbor, newCost))
        }
      }
    }

    return distances
  }

  // DFS algorithm to traverse back from the end point via the best paths.
  private fun traverseBackViaBestPaths(
    distances: Map<Pose, Int>,
    visited: MutableSet<Pose>,
    pose: Pose,
  ) {
    if (pose in visited) return
    visited.add(pose)

    // Check all the poses from which the current pose can be reached.
    pose.reverseNeighborsWithCost().forEach { (neighbor, edgeCost) ->
      // If the neighbor pose is at the best distance from the current pose, traverse back via it.
      if (distances[neighbor] == distances[pose]!! - edgeCost) {
        traverseBackViaBestPaths(distances, visited, neighbor)
      }
    }
  }
}

fun main() = solution.run()
