package lib

data class Point(val x: Int, val y: Int) {
  operator fun plus(other: Point): Point = Point(x + other.x, y + other.y)

  operator fun minus(other: Point): Point = this + (-other)

  operator fun unaryMinus(): Point = Point(-x, -y)

  operator fun times(scale: Int): Point = Point(x * scale, y * scale)

  fun adjacents(): List<Point> = listOf(left(), right(), up(), down())

  private fun left(): Point = this - X_DIRECTION

  private fun right(): Point = this + X_DIRECTION

  private fun up(): Point = this - Y_DIRECTION

  private fun down(): Point = this + Y_DIRECTION

  private companion object {
    val X_DIRECTION = Point(1, 0)
    val Y_DIRECTION = Point(0, 1)
  }
}

data class Grid<T>(val grid: List<List<T>>) {
  private val height: Int = grid.size
  private val width: Int = grid.map(List<T>::size).toSet().single()

  fun transposed(): Grid<T> = Grid(
    List(width) { x ->
      List(height) { y ->
        grid[y][x]
      }
    }
  )

  fun flipVertically(): Grid<T> = Grid(grid.reversed())

  fun flipHorizontally(): Grid<T> = Grid(grid.map(List<T>::reversed))

  fun rotateCW(): Grid<T> = flipVertically().transposed()

  fun rotateCCW(): Grid<T> = flipHorizontally().transposed()

  fun rotate180(): Grid<T> = flipVertically().flipHorizontally()

  fun <R> mapIndexed(transform: (Point, T) -> R): Grid<R> = Grid(
    List(height) { y ->
      List(width) { x ->
        transform(Point(x, y), grid[y][x])
      }
    }
  )

  fun <R> map(transform: (T) -> R): Grid<R> =
    mapIndexed { _, value -> transform(value) }

  fun forEachIndexed(transform: (Point, T) -> Unit) {
    mapIndexed(transform)
  }

  fun forEach(transform: (T) -> Unit) {
    map(transform)
  }

  fun zip(other: Grid<T>, transform: (T, T) -> T): Grid<T> = Grid(
    grid.zip(other.grid) { x, y -> x.zip(y, transform) }
  )

  fun count(predicate: (T) -> Boolean): Int = grid.sumOf { it.count(predicate) }

  operator fun get(point: Point): T = grid[point.y][point.x]

  operator fun contains(point: Point): Boolean =
    (point.x in (0 until width)) && (point.y in (0 until height))

  fun adjacents(point: Point): List<Point> =
    point.adjacents().filter { it in this }

  companion object {
    fun <T : Comparable<T>> Grid<T>.max(): T = grid.maxOf { it.max() }

    fun <T : Comparable<T>> Grid<T>.min(): T = grid.minOf { it.min() }
  }
}