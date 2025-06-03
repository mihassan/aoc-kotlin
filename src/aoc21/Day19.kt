@file:Suppress("PackageDirectoryMismatch")

package aoc21.day19

import aoc21.day19.Scanner.Companion.placeScanners
import kotlin.math.abs
import lib.Combinatorics.combinations
import lib.Combinatorics.permutations
import lib.Combinatorics.permutationsWithReplacement
import lib.Solution

/**
 * Represents a point in 3D space with integer coordinates (x, y, z).
 * Provides various operations such as addition, subtraction, scaling,
 * distance calculation, and dot product.
 */
private data class Point3D(val x: Int, val y: Int, val z: Int) {
  operator fun plus(other: Point3D): Point3D = Point3D(x + other.x, y + other.y, z + other.z)

  operator fun minus(other: Point3D): Point3D = Point3D(x - other.x, y - other.y, z - other.z)

  /**
   * Multiplies this point by a scalar value, scaling each coordinate.
   * This is useful for scaling the point in 3D space.
   */
  operator fun times(scalar: Int): Point3D = Point3D(x * scalar, y * scalar, z * scalar)

  /**
   * Calculates the Manhattan distance between this point and another point in 3D space.
   */
  infix fun distance(other: Point3D): Int = abs(x - other.x) + abs(y - other.y) + abs(z - other.z)

  /**
   * Calculates the squared distance between this point and another point in 3D space.
   * This is useful for comparing distances without taking the square root.
   */
  infix fun dot(other: Point3D): Int = x * other.x + y * other.y + z * other.z

  companion object {
    val ORIGIN = Point3D(0, 0, 0)

    fun parse(pointStr: String): Point3D {
      val (x, y, z) = pointStr.split(",", limit = 3).map { it.toInt() }
      return Point3D(x, y, z)
    }
  }
}

private typealias Vector3D = Point3D

/**
 * Represents a rotation in 3D space using three orthogonal vectors: right, up, and front.
 * These vectors define the orientation of the coordinate system.
 */
private data class Rotation(val right: Vector3D, val up: Vector3D, val front: Vector3D) {
  /**
   * Applies this rotation to a point in 3D space.
   * The point is transformed by the rotation matrix defined by this rotation.
   */
  operator fun invoke(point: Point3D): Point3D =
    Point3D(
      x = right.x * point.x + up.x * point.y + front.x * point.z,
      y = right.y * point.x + up.y * point.y + front.y * point.z,
      z = right.z * point.x + up.z * point.y + front.z * point.z
    )

  /**
   * Returns the inverse of this rotation by transposing the rotation matrix.
   */
  val inverse
    get(): Rotation =
      Rotation(
        right = Vector3D(right.x, up.x, front.x),
        up = Vector3D(right.y, up.y, front.y),
        front = Vector3D(right.z, up.z, front.z)
      )

  /**
   * Transforms a point from local coordinates to global coordinates using this rotation.
   * There is no translation involved, only rotation.
   * This is the inverse of the globalToLocal function.
   */
  fun localToGlobal(point: Point3D): Point3D = this(point)

  /**
   * Transforms a point from global coordinates to local coordinates using this rotation.
   * There is no translation involved, only rotation.
   * This is the inverse of the localToGlobal function.
   */
  fun globalToLocal(point: Point3D): Point3D = inverse(point)

  /**
   * Combines this rotation with another rotation using matrix multiplication.
   * The result is a new rotation that applies the other rotation first, then this one.
   */
  operator fun times(other: Rotation): Rotation {
    val t = other.inverse
    return Rotation(
      right = Vector3D(x = right dot t.right, y = right dot t.up, z = right dot t.front),
      up = Vector3D(x = up dot t.right, y = up dot t.up, z = up dot t.front),
      front = Vector3D(x = front dot t.right, y = front dot t.up, z = front dot t.front)
    )
  }

  /**
   * Checks if this rotation is right-handed which can be achieved without reflecting any axes.
   * A right-handed rotation has a positive determinant, which can be checked
   * using the scalar triple product of the right, up, and front vectors.
   */
  fun isRightHanded(): Boolean =
    right.x * (up.y * front.z - up.z * front.y) +
      right.y * (up.z * front.x - up.x * front.z) +
      right.z * (up.x * front.y - up.y * front.x) > 0

  companion object {
    val IDENTITY = Rotation(Vector3D(1, 0, 0), Vector3D(0, 1, 0), Vector3D(0, 0, 1))

    /**
     * Generates all possible rotations in 3D space by permuting the right, up, and front vectors
     * and applying all combinations of flipping each vector.
     * The result is a list of all valid right-handed rotations.
     * This includes 24 unique rotations.
     */
    val ALL_ROTATIONS: List<Rotation> = buildList {
      permutations(setOf(IDENTITY.right, IDENTITY.up, IDENTITY.front)).forEach {
        val (r, u, f) = it
        permutationsWithReplacement(setOf(1, -1), 3).forEach { (rs, us, fs) ->
          val rot = Rotation(right = r * rs, up = u * us, front = f * fs)
          if (rot.isRightHanded())
            add(rot)
        }
      }
    }
  }
}

/**
 * Represents a reference frame in 3D space, defined by a position and a rotation.
 * The position is the origin of the reference frame, and the rotation defines
 * how local coordinates are transformed to global coordinates.
 */
private data class Reference(val position: Point3D, val rotation: Rotation) {
  /**
   * Transforms a point from local coordinates to global coordinates using this reference frame.
   * The point is first rotated by the rotation and then translated by the position.
   */
  fun localToGlobal(point: Point3D): Point3D = rotation.localToGlobal(point) + position

  /**
   * Combines this reference frame within another reference frame.
   * The position is transformed from local to global coordinates using this reference,
   * and the rotations are combined using matrix multiplication.
   */
  infix fun within(other: Reference): Reference {
    val newPosition = other.localToGlobal(position)
    val newRotation = rotation * other.rotation
    return Reference(newPosition, newRotation)
  }

  companion object {
    val IDENTITY = Reference(Point3D.ORIGIN, Rotation.IDENTITY)
  }
}

/**
 * Represents a beacon in 3D space, which is simply a point in 3D space.
 * This type alias is used for clarity in the context of scanners and beacons.
 */
private typealias Beacon = Point3D

/**
 * Represents a scanner that detects beacons in 3D space.
 * Each scanner has a list of beacons it has detected.
 * The scanner can check for potential overlaps with other scanners
 * and can place itself relative to another scanner based on shared beacons.
 */
private data class Scanner(val beacons: List<Beacon>) {
  /**
   * Checks if this scanner potentially overlaps with another scanner.
   * The heuristic is based on the number of unique distances.
   * If 12 beacons are shared, they must have at least 66 unique distances.
   */
  infix fun potentiallyOverlapsWith(other: Scanner): Boolean =
    (beaconPairDistances() intersect other.beaconPairDistances()).size >= 66

  /**
   * Attempts to place this scanner with respect to another scanner.
   * It finds a reference frame that aligns the beacons of this scanner.
   * It checks all pairs of beacons from both scanners and all possible rotations.
   * If at least 12 beacons match after transformation,
   * it returns a reference frame that describes the position and rotation.
   */
  fun placeWithRelationTo(other: Scanner, otherReference: Reference): Reference? {
    for (beacon in beacons) {
      for (otherBeacon in other.beacons) {
        for (rotation in Rotation.ALL_ROTATIONS) {
          // Calculate the candidate reference by aligning the current beacon with the other beacon.
          val candidateReference = Reference(otherBeacon - rotation.localToGlobal(beacon), rotation)
          // Transform all beacons of this scanner to global coordinates using the candidate reference.
          val transformedBeacons = beacons.map(candidateReference::localToGlobal)
          // At least 12 beacons must match for 2 scanners to be considered aligned.
          val commonBeacons = transformedBeacons intersect other.beacons.toSet()
          if (commonBeacons.size >= 12) {
            return candidateReference within otherReference
          }
        }
      }
    }
    // If no valid placement is found, return null.
    return null
  }

  private fun beaconPairs(): Set<Pair<Beacon, Beacon>> =
    combinations(beacons.toSet(), 2).map { it.first() to it.last() }.toSet()

  private fun beaconPairDistances(): List<Int> =
    beaconPairs().map { (a, b) -> a distance b }

  companion object {
    fun parseScanner(scannerStr: String): Scanner =
      Scanner(scannerStr.lines().drop(1).map { Point3D.parse(it) })

    fun parseScanners(scannersStr: String): List<Scanner> =
      scannersStr.split("\n\n").map { parseScanner(it) }

    /**
     * Constructs a potential overlap graph for the scanners.
     * This graph maps each scanner index to a set of indices of scanners.
     * It indicates which scanners potentially overlap with each other.
     * This heuristic is based on the assumption that if two scanners potentially overlap,
     * they share at least 12 beacons and have at least 66 unique distances.
     * This is used to efficiently find which scanners can be placed relative to each other.
     * However, it does not guarantee that the scanners can be placed relative to each other.
     */
    fun List<Scanner>.potentialOverlapGraph(): Map<Int, Set<Int>> {
      val graph = mutableMapOf<Int, MutableSet<Int>>()
      for (i in indices) {
        graph[i] = mutableSetOf()
        for (j in indices) {
          if (i != j && this[i] potentiallyOverlapsWith this[j]) {
            graph[i]!!.add(j)
          }
        }
      }
      return graph
    }

    /**
     * Places all scanners in a global reference frame.
     * It uses a depth-first search (DFS) to traverse the potential overlap graph
     * and place each scanner relative to the previously placed scanners.
     * The first scanner is placed at the origin with an identity rotation.
     * Each subsequent scanner is placed using the relation to its neighbors.
     * The result is a list of references that describe the position and rotation
     * of each scanner in the global reference frame.
     */
    fun List<Scanner>.placeScanners(): List<Reference> {
      val graph = potentialOverlapGraph()
      val references = mutableMapOf(0 to Reference.IDENTITY)

      fun dfs(scannerIndex: Int) {
        val scanner = this[scannerIndex]
        val reference = references[scannerIndex] ?: Reference.IDENTITY
        val neighbourIndices = graph[scannerIndex] ?: return

        for (neighborIndex in neighbourIndices) {
          if (neighborIndex in references) continue
          val neighbourReference = this[neighborIndex].placeWithRelationTo(scanner, reference)

          if (neighbourReference != null) {
            references[neighborIndex] = neighbourReference
            dfs(neighborIndex)
          }
        }
      }

      dfs(0)

      return List(size) { index -> references[index] ?: error("Could not place $index") }
    }
  }
}

private typealias Input = List<Scanner>
private typealias Output = Int

private val solution = object : Solution<Input, Output>(2021, "Day19") {
  override fun parse(input: String): Input = Scanner.parseScanners(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.zip(input.placeScanners()).flatMap { (s, p) -> s.beacons.map(p::localToGlobal) }
      .distinct().size

  override fun part2(input: Input): Output {
    val positions = input.placeScanners().map { it.position }
    return positions.flatMap { p1 ->
      positions.map { p2 -> p1 distance p2 }
    }.maxOrNull() ?: error("No maximum distance found")
  }
}

fun main() = solution.run()
