@file:Suppress("PackageDirectoryMismatch")

package aoc22.day16

import kotlin.math.max
import kotlin.math.min
import lib.Collections.headTail
import lib.Solution
import lib.Strings.extractLongs

typealias RoomLabel = String

typealias Distance = Long

data class Room(val label: RoomLabel, val flowRate: Long, val tunnels: Map<RoomLabel, Distance>) {
  val hasPositiveFlow by lazy { flowRate > 0 }
  val initialRoom by lazy { label == INITIAL_ROOM_LABEL }

  companion object {
    private const val INITIAL_ROOM_LABEL = "AA"

    fun parse(line: String): Room {
      val flowRate = line.extractLongs().single()
      val (label, connectedRooms) = Regex("[A-Z]{2}")
        .findAll(line)
        .map(MatchResult::value)
        .toList()
        .headTail()
      val tunnels = connectedRooms.associateWith { 1L }
      return Room(label!!, flowRate, tunnels)
    }
  }
}

typealias Input = Map<RoomLabel, Room>

typealias Output = String

private val solution = object : Solution<Input, Output>(2022, "Day16") {
  override fun parse(input: String): Input =
    input.lines().map { Room.parse(it) }.associateBy { it.label }

  override fun format(output: Output): String {
    return output
  }

  override fun part1(input: Input): Output {
    val selectiveRooms = getSelectiveRooms(input).associateBy { it.label }
    return "${calculateMaxPressureRelease(selectiveRooms)}"
  }

  override fun part2(input: Input): Output {
    TODO()
  }
}


/**
 * Use Floyd–Warshall algorithm to compute the shortest path distance between all pair of rooms.
 */
private fun getShortestPaths(input: Input): Map<Pair<RoomLabel, RoomLabel>, Long> {
  val distances = mutableMapOf<Pair<RoomLabel, RoomLabel>, Long>().withDefault { 1_000_000_000 }

  input.keys.forEach {
    distances[it to it] = 0
  }

  input.forEach { (fromLabel, fromRoom) ->
    fromRoom.tunnels.forEach { (toLabel, distance) ->
      distances[fromLabel to toLabel] = distance
    }
  }

  input.keys.forEach { k ->
    input.keys.forEach { i ->
      input.keys.forEach { j ->
        distances[i to j] = min(
          distances.getValue(i to j),
          distances.getValue(i to k) + distances.getValue(k to j)
        )
      }
    }
  }

  return distances
}

/**
 * Get rooms with positive flows or the initial room and shortest distance to all other rooms.
 */
private fun getSelectiveRooms(input: Input): List<Room> {
  val shortestPaths = getShortestPaths(input)

  val selectedRoomLabels = input.values
    .filter { it.initialRoom || it.hasPositiveFlow }
    .map { it.label }

  return selectedRoomLabels.map { fromLabel ->
    val tunnels = selectedRoomLabels.associateWith { toLabel ->
      shortestPaths[fromLabel to toLabel]!!
    }
    input[fromLabel]!!.copy(tunnels = tunnels)
  }
}

private fun calculateMaxPressureRelease(input: Input): Long {
  var result = 0L

  data class Location(val roomLabel: RoomLabel, val remainingDistance: Distance) {
    val room by lazy { input[roomLabel]!! }
  }

  fun releasePressure(
    minutesLeft: Long,
    pressureReleased: Map<RoomLabel, Long>,
    location: Location,
  ): Map<RoomLabel, Long> {
    if (location.remainingDistance > 0L || location.roomLabel in pressureReleased)
      return pressureReleased

    val nextPressureReleased =
      pressureReleased + (location.roomLabel to (location.room.flowRate * minutesLeft))
    result = max(result, nextPressureReleased.values.sum())
    return nextPressureReleased
  }

  fun markRoomAsSeen(
    seenRooms: Set<RoomLabel>,
    location: Location,
  ): Set<RoomLabel> {
    return seenRooms + location.roomLabel
  }

  fun openLocations(
    seenRooms: Set<RoomLabel>,
    minutesLeft: Long,
    location: Location,
  ): List<Location> {
    return location.room.tunnels
      .filter { (nextRoomLabel, distance) ->
        nextRoomLabel !in seenRooms && minutesLeft > distance + 1
      }
      .map { (nextRoomLabel, distance) ->
        Location(nextRoomLabel, distance + 1)
      }
  }

  fun move(
    minutesLeft: Long,
    location: Location,
  ): Pair<Long, Location> {
    return (minutesLeft - location.remainingDistance) to location.copy(remainingDistance = 0L)
  }

  fun dfs(
    seenRooms: Set<RoomLabel>,
    minutesLeft: Long,
    pressureReleased: Map<RoomLabel, Long>,
    location: Location,
  ) {
    val nextPressureReleased = releasePressure(minutesLeft, pressureReleased, location)
    val nextSeenRooms = markRoomAsSeen(seenRooms, location)

    if (location.remainingDistance > 0) {
      val (nextMinutesLeft, nextLocation) = move(minutesLeft, location)
      dfs(nextSeenRooms, nextMinutesLeft, nextPressureReleased, nextLocation)
    } else {
      val nextLocationChoices = openLocations(nextSeenRooms, minutesLeft, location)
      nextLocationChoices.forEach { nextLocation ->
        dfs(nextSeenRooms, minutesLeft, nextPressureReleased, nextLocation)
      }
    }
  }

  dfs(emptySet(), 30L, emptyMap(), Location("AA", 0L))

  return result
}

fun main() = solution.run()
