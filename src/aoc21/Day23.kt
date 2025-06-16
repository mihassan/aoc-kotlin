@file:Suppress("PackageDirectoryMismatch")

package aoc21.day23

import aoc21.day23.Room.Companion.toRoom
import aoc21.day23.Room.Companion.isAboveRoom
import aoc21.day23.Room.Companion.isInRoom
import java.util.PriorityQueue
import lib.Direction
import lib.Point
import lib.Solution

private typealias Energy = Long

private enum class Amphipod(val char: Char, val energy: Energy) {
  A('A', 1L), B('B', 10L), C('C', 100L), D('D', 1000L);

  companion object {
    fun parse(char: Char): Amphipod? = entries.find { it.char == char }
  }
}

private data class Room(val type: Amphipod) {
  companion object {
    fun Point.toRoom(): Room? {
      val amphipod = when (x) {
        3 -> Amphipod.A
        5 -> Amphipod.B
        7 -> Amphipod.C
        9 -> Amphipod.D
        else -> return null
      }
      return Room(amphipod)
    }

    fun Point.isInRoom(): Boolean = y > 1 && toRoom() != null

    fun Point.isAboveRoom(): Boolean = y == 1 && toRoom() != null
  }
}

private sealed interface Cell {
  val amphipod: Amphipod?

  data class Hallway(override val amphipod: Amphipod?) : Cell
  data class RoomCell(override val amphipod: Amphipod?, val room: Room) : Cell

  fun hasCorrectAmphipod(): Boolean = when (this) {
    is RoomCell -> amphipod == room.type
    else -> !isOccupied()
  }

  fun isOccupied(): Boolean = amphipod != null

  fun isInRoom(room: Room): Boolean = this is RoomCell && this.room == room

  fun removeAmphipod(): Cell = when (this) {
    is Hallway -> Hallway(null)
    is RoomCell -> RoomCell(null, room)
  }

  fun placeAmphipod(amphipod: Amphipod): Cell = when (this) {
    is Hallway -> Hallway(amphipod)
    is RoomCell -> RoomCell(amphipod, room)
  }

  companion object {
    fun parse(position: Point, char: Char): Cell? {
      val room = position.toRoom()
      val amphipod = Amphipod.parse(char)

      return when {
        char == '.' && !position.isAboveRoom() -> Hallway(null)
        room != null && amphipod != null -> RoomCell(amphipod, room)
        else -> null
      }
    }
  }
}

private data class Diagram(val cells: Map<Point, Cell>) {
  fun isOrganized(): Boolean = cells.values.all(Cell::hasCorrectAmphipod)

  fun allPairs(): Sequence<Pair<Point, Point>> = sequence {
    cells.keys.forEach { from ->
      cells.keys.forEach { to ->
        yield(from to to)
      }
    }
  }

  fun move(from: Point, to: Point): Pair<Diagram, Energy>? {
    val fromCell = cells[from] ?: return null
    val toCell = cells[to] ?: return null
    val amphipod = fromCell.amphipod ?: return null

    // Invalid move: cannot move to the same cell
    if (from == to) return null

    // Invalid move: target cell already has an amphipod
    if (toCell.isOccupied()) return null

    // Invalid move: cannot move between hallways directly
    if (fromCell is Cell.Hallway && toCell is Cell.Hallway) return null

    // Invalid move: should not move out of the correct room unless there are other types of amphipods in the room
    if (fromCell is Cell.RoomCell && allCorrectAmphipodsInRoom(fromCell.room)) return null

    if (toCell is Cell.RoomCell) {
      // Invalid move: must move to the correct room
      if (toCell.room.type != amphipod) return null

      // Invalid move: must move to a room containing the same type of amphipods
      if (!allCorrectAmphipodsInRoom(toCell.room)) return null
    }

    val path: List<Point> = pathBetween(from, to)
    if (path.any { cells[it]?.isOccupied() == true }) {
      // Invalid move: path is blocked by another amphipod
      return null
    }

    val newCells =
      cells + mapOf(from to fromCell.removeAmphipod(), to to toCell.placeAmphipod(amphipod))
    return Diagram(newCells) to (amphipod.energy * path.size)
  }

  fun amendDiagram(): Diagram {
    val newCells =
      cells.mapKeys {
        if (it.key.y == 3)
          it.key.copy(y = 5)
        else
          it.key
      }.toMutableMap()

    newCells += mapOf(
      Point(3, 3) to Cell.RoomCell(Amphipod.D, Room(Amphipod.A)),
      Point(5, 3) to Cell.RoomCell(Amphipod.C, Room(Amphipod.B)),
      Point(7, 3) to Cell.RoomCell(Amphipod.B, Room(Amphipod.C)),
      Point(9, 3) to Cell.RoomCell(Amphipod.A, Room(Amphipod.D)),
      Point(3, 4) to Cell.RoomCell(Amphipod.D, Room(Amphipod.A)),
      Point(5, 4) to Cell.RoomCell(Amphipod.B, Room(Amphipod.B)),
      Point(7, 4) to Cell.RoomCell(Amphipod.A, Room(Amphipod.C)),
      Point(9, 4) to Cell.RoomCell(Amphipod.C, Room(Amphipod.D)),
    )

    return Diagram(newCells)
  }

  private fun allCellsInRoom(room: Room): List<Cell> = cells.values.filter { it.isInRoom(room) }

  private fun allAmphipodsInRoom(room: Room): List<Amphipod> =
    allCellsInRoom(room).mapNotNull { it.amphipod }

  private fun allCorrectAmphipodsInRoom(room: Room): Boolean =
    allAmphipodsInRoom(room).all { it == room.type }

  private fun pathBetween(from: Point, to: Point): List<Point> {
    val path = mutableListOf<Point>()
    var curr = from

    // If the amphipod is in a room, try to move it out to the hallway first.
    while (curr.isInRoom()) {
      curr = curr.move(Direction.UP)
      path += curr
    }

    // Now move horizontally to the target position.
    while (curr.x != to.x) {
      curr = if (curr.x < to.x) curr.move(Direction.RIGHT) else curr.move(Direction.LEFT)
      path += curr
    }

    // Finally, go down to the target position.
    while (curr.y < to.y) {
      curr = curr.move(Direction.DOWN)
      path += curr
    }

    return path
  }


  companion object {
    fun parse(diagramStr: String): Diagram {
      val cells = mutableMapOf<Point, Cell>()

      diagramStr.lines().forEachIndexed { y, line ->
        line.forEachIndexed { x, ch ->
          val point = Point(x, y)

          Cell.parse(point, ch)?.let {
            cells[point] = it
          }
        }
      }

      return Diagram(cells)
    }
  }
}

private typealias Input = Diagram
private typealias Output = Energy

private val solution = object : Solution<Input, Output>(2021, "Day23") {
  override fun parse(input: String): Input = Diagram.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    val cost = mutableMapOf<Diagram, Energy>()
    val queue = PriorityQueue<Pair<Energy, Diagram>>(compareBy { it.first })

    queue.add(0L to input)

    while (queue.isNotEmpty()) {
      val (currentEnergy, currentDiagram) = queue.poll()

      // If the current diagram is organized, we can update the minimum energy.
      if (currentDiagram.isOrganized()) return currentEnergy

      // If we have already found a cheaper way to reach this diagram, skip it.
      if (cost[currentDiagram] != null) continue
      cost[currentDiagram] = currentEnergy

      // Explore all possible moves from the current diagram.
      currentDiagram.allPairs().forEach { (from, to) ->
        currentDiagram.move(from, to)?.let { (newDiagram, energy) ->
          queue.add(currentEnergy + energy to newDiagram)
        }
      }
    }

    return 0L
  }

  override fun part2(input: Input): Output = part1(input.amendDiagram())
}

fun main() = solution.run()
