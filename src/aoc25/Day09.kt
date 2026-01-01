@file:Suppress("PackageDirectoryMismatch")

package aoc25.day09

import lib.Adjacency
import lib.Path
import lib.Point
import lib.Solution
import lib.Strings.extractInts

private typealias Polygon = List<Point>

private typealias Input = Polygon
private typealias Output = Long

/**
 * Represents an axis-aligned rectangle defined by two corner points.
 * Provides area calculation with inclusive boundaries.
 */
private data class Rectangle(val corner1: Point, val corner2: Point) {
  val minX: Int get() = minOf(corner1.x, corner2.x)
  val maxX: Int get() = maxOf(corner1.x, corner2.x)
  val minY: Int get() = minOf(corner1.y, corner2.y)
  val maxY: Int get() = maxOf(corner1.y, corner2.y)

  val xRange: IntRange get() = minX..maxX
  val yRange: IntRange get() = minY..maxY

  val area: Long get() = (maxX - minX + 1L) * (maxY - minY + 1L)

  /** Returns all points within this rectangle. */
  fun allPoints(): Sequence<Point> = xRange.asSequence().flatMap { x ->
    yRange.asSequence().map { y -> Point(x, y) }
  }
}

/**
 * Coordinate compression maps original coordinates to a compact integer range.
 * This allows efficient grid-based algorithms on sparse point sets.
 *
 * The compressed coordinates are 1-indexed to leave room for a padding border.
 */
private class CoordinateCompressor(points: List<Point>) {
  private val xMapping: Map<Int, Int> = createMapping(points.map { it.x })
  private val yMapping: Map<Int, Int> = createMapping(points.map { it.y })

  /** Grid width including 1-cell padding on each side. */
  val gridWidth: Int = xMapping.size + 2

  /** Grid height including 1-cell padding on each side. */
  val gridHeight: Int = yMapping.size + 2

  fun compress(point: Point): Point = Point(xMapping.getValue(point.x), yMapping.getValue(point.y))

  fun compressAll(points: List<Point>): List<Point> = points.map(::compress)

  companion object {
    /** Maps sorted unique values to 1-indexed integers (leaving index 0 for padding). */
    private fun createMapping(values: List<Int>): Map<Int, Int> =
      values.toSortedSet().withIndex().associate { (index, value) -> value to index + 1 }
  }
}

/**
 * Represents the interior/exterior classification of a polygon on a grid.
 * Uses flood fill from the origin to identify exterior points.
 */
private class PolygonInterior(
  compressedVertices: List<Point>,
  gridWidth: Int,
  gridHeight: Int,
) {
  private val boundary: Set<Point> = computePolygonBoundary(compressedVertices)
  private val exterior: Set<Point> = floodFillExterior(gridWidth, gridHeight, boundary)

  /** Checks if a point is inside the polygon (including boundary). */
  fun isInside(point: Point): Boolean = point !in exterior

  /** Checks if all points of a rectangle are inside the polygon. */
  fun containsRectangle(rectangle: Rectangle): Boolean = rectangle.allPoints().all(::isInside)

  private fun computePolygonBoundary(vertices: List<Point>): Set<Point> {
    val closedPolygon = vertices + vertices.first()
    return Path(closedPolygon).expand().toSet()
  }

  private fun floodFillExterior(
    width: Int,
    height: Int,
    boundary: Set<Point>,
  ): Set<Point> = buildSet {
    val origin = Point(0, 0)
    add(origin)
    val queue = ArrayDeque<Point>().apply { add(origin) }

    while (queue.isNotEmpty()) {
      val current = queue.removeFirst()
      current.adjacents(Adjacency.ORTHOGONAL).filter { it.isWithinBounds(width, height) }
        .filter { it !in this && it !in boundary }.forEach { neighbor ->
          add(neighbor)
          queue.add(neighbor)
        }
    }
  }

  private fun Point.isWithinBounds(width: Int, height: Int): Boolean =
    x in 0 until width && y in 0 until height
}

/** Generates all unique pairs of elements from the list. */
private fun <T> List<T>.uniquePairs(): Sequence<Pair<T, T>> = sequence {
  for (i in indices) {
    for (j in i + 1 until size) {
      yield(this@uniquePairs[i] to this@uniquePairs[j])
    }
  }
}

/** Creates a rectangle from two points. */
private infix fun Point.rectangleTo(other: Point): Rectangle = Rectangle(this, other)

private val solution = object : Solution<Polygon, Output>(2025, "Day09") {

  override fun parse(input: String): Polygon = input.lines().map { line ->
    val (x, y) = line.extractInts()
    Point(x, y)
  }

  override fun format(output: Output): String = output.toString()

  /**
   * Part 1: Find the maximum area rectangle using any two polygon vertices as corners.
   * No constraint on whether the rectangle is inside the polygon.
   */
  override fun part1(input: Input): Output =
    input.uniquePairs().map { (p1, p2) -> p1 rectangleTo p2 }.maxOf { it.area }

  /**
   * Part 2: Find the maximum area rectangle that lies completely inside the polygon.
   *
   * Approach:
   * 1. Compress coordinates to create a manageable grid
   * 2. Use flood fill to classify interior vs exterior points
   * 3. Check all candidate rectangles (defined by vertex pairs)
   */
  override fun part2(input: Input): Output {
    val compressor = CoordinateCompressor(input)
    val compressedVertices = compressor.compressAll(input)
    val interior = PolygonInterior(
      compressedVertices,
      compressor.gridWidth,
      compressor.gridHeight,
    )

    // Pair original vertices with compressed vertices for area calculation
    val vertexPairs = input.zip(compressedVertices)

    return vertexPairs.uniquePairs().filter { (pair1, pair2) ->
        val compressedRect = pair1.second rectangleTo pair2.second
        interior.containsRectangle(compressedRect)
      }.maxOfOrNull { (pair1, pair2) ->
        val originalRect = pair1.first rectangleTo pair2.first
        originalRect.area
      } ?: 0L
  }
}

fun main() = solution.run()
