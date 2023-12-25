@file:Suppress("PackageDirectoryMismatch")

package aoc23.day10

import lib.Direction
import lib.Direction.*
import lib.Grid
import lib.Point
import lib.Solution

enum class Tile(val symbol: Char, val connections: Set<Direction>) {
  GROUND('.', emptySet()),
  VERTICAL('|', setOf(UP, DOWN)),
  HORIZONTAL('-', setOf(LEFT, RIGHT)),
  NORTH_EAST('L', setOf(UP, RIGHT)),
  NORTH_WEST('J', setOf(UP, LEFT)),
  SOUTH_WEST('7', setOf(DOWN, LEFT)),
  SOUTH_EAST('F', setOf(DOWN, RIGHT)),
  START('S', setOf(UP, DOWN, LEFT, RIGHT));

  companion object {
    fun parse(symbol: Char): Tile {
      return values().find { it.symbol == symbol } ?: error("Unknown symbol: $symbol")
    }
  }
}

data class Connection(val from: Point, val to: Point, val direction: Direction) {
  fun reversed(): Connection = Connection(to, from, direction.turnAround())
}

enum class Orientation { CW, CCW }

/**
 * A closed pipe is a sequence of connections that starts and ends at the same point. We use
 * connections instead of points to represent the pipe as we need to know the direction of the pipe.
 * The original tiles are not important to keep track of. However, if needed, we can always get the
 * original tiles from the grid.
 */
data class Pipe(val connections: List<Connection>) {
  val length: Int = connections.size

  /**
   * Fix the orientation of the pipe. The pipe is either clockwise or counter-clockwise.
   */
  fun fixOrientation(orientation: Orientation): Pipe =
    if (orientation == this.checkOrientation())
      this
    else
      reversed()

  /**
   * Reverse the pipe by traversing the connections in reverse order and also by reversing each
   * individual connection.
   */
  private fun reversed() = Pipe(connections.reversed().map(Connection::reversed))

  /**
   * Check the orientation of the pipe. The pipe is either clockwise or counter-clockwise.
   * We start with the left most connection on the top most row. Then the connection must be either
   * RIGHT or DOWN as otherwise the connection is not left-most connection in top-most row. If the
   * connection is RIGHT, then the pipe orientation is clockwise, otherwise it is counter-clockwise.
   */
  private fun checkOrientation(): Orientation =
    if (findTopLeftConnection().direction == RIGHT) Orientation.CW else Orientation.CCW

  /**
   * Find the left most connection on the top most row. The connection must be either RIGHT or DOWN.
   * Also, the connection must start with a tile of type START or SOUTH_EAST as otherwise there is
   * another tile either on the left or on top making it not the left most connection on the top
   * most row.
   */
  private fun findTopLeftConnection() = connections.minWith(compareBy({ it.from.y }, { it.from.x }))
}

data class Field(val grid: Grid<Tile>) {
  private val start: Point by lazy { grid.indexOf(Tile.START) }

  fun findPipe(): Pipe = Pipe(buildList {
    var curr = connections(start).first()
    add(curr)

    while (curr.to != start) {
      val next = connections(curr.to).first { it.to != curr.from }
      curr = next
      add(curr)
    }
  })

  private fun connections(from: Point): List<Connection> =
    possibleConnections(from).filter { connection ->
      connection.to in grid && connection.direction.turnAround() in grid[connection.to].connections
    }

  private fun possibleConnections(from: Point): List<Connection> =
    grid[from].connections.map {
      Connection(from, from.move(it), it)
    }

  companion object {
    fun parse(input: String): Field = Field(Grid.parse(input).map { Tile.parse(it) })
  }
}

typealias Input = Field

typealias Output = Int

private val solution = object : Solution<Input, Output>(2023, "Day10") {
  override fun parse(input: String): Input = Field.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.findPipe().length / 2

  override fun part2(input: Input): Output {
    // Traverse the pipe clockwise while looking on the right side of the pipe. If there is a tile
    // on the right side of the pipe, then we are inside the pipe. Otherwise, we are outside the
    // pipe. Then we run a BFS from the inside of the pipe to find all the points inside the pipe.

    val pipe = input.findPipe().fixOrientation(Orientation.CW)
    val pointsOnPipe = pipe.connections.map { it.from }.toSet()

    // Find all the points adjacent to the pipe on the right side. These are the points that are
    // inside the pipe.
    val innerPoints = pipe.connections.flatMap { connection ->
      listOf(connection.from, connection.to).mapNotNull { pointOnPipe ->
        val rightDir = connection.direction.turnRight()
        val innerPoint = pointOnPipe.move(rightDir)
        innerPoint.takeIf { it in input.grid && it !in pointsOnPipe }
      }
    }

    // Run a BFS from the inside of the pipe to find all the points inside the pipe.
    val queue = ArrayDeque(innerPoints)
    val visited = pointsOnPipe.toMutableSet()

    while (queue.isNotEmpty()) {
      val point = queue.removeFirst()
      if (point in input.grid && point !in visited) {
        visited.add(point)
        queue.addAll(input.grid.adjacents(point))
      }
    }

    // The number of points inside the pipe is the number of visited points minus the number of
    // points on the pipe.
    return visited.size - pointsOnPipe.size
  }
}

fun main() = solution.run()
