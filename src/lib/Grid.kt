package lib

enum class Direction {
  RIGHT, DOWN, LEFT, UP
}

enum class Adjacency {
  HORIZONTAL, VERTICAL, ORTHOGONAL, DIAGONAL, ALL
}

data class Point(val x: Int, val y: Int) : Comparable<Point> {
  operator fun plus(other: Point): Point = Point(x + other.x, y + other.y)

  operator fun minus(other: Point): Point = this + (-other)

  operator fun unaryMinus(): Point = Point(-x, -y)

  operator fun times(scale: Int): Point = Point(x * scale, y * scale)

  operator fun div(scale: Int): Point = Point(x / scale, y / scale)

  fun adjacents(adjacency: Adjacency = Adjacency.ORTHOGONAL): List<Point> = when (adjacency) {
    Adjacency.HORIZONTAL -> listOf(left(), right())
    Adjacency.VERTICAL -> listOf(up(), down())
    Adjacency.ORTHOGONAL -> adjacents(Adjacency.HORIZONTAL) + adjacents(Adjacency.VERTICAL)
    Adjacency.DIAGONAL -> listOf(upLeft(), upRight(), downLeft(), downRight())
    Adjacency.ALL -> adjacents(Adjacency.ORTHOGONAL) + adjacents(Adjacency.DIAGONAL)
  }

  fun move(direction: Direction): Point = when (direction) {
    Direction.RIGHT -> right()
    Direction.DOWN -> down()
    Direction.LEFT -> left()
    Direction.UP -> up()
  }

  fun left(): Point = this - X_DIRECTION

  fun right(): Point = this + X_DIRECTION

  fun up(): Point = this - Y_DIRECTION

  fun down(): Point = this + Y_DIRECTION

  fun upLeft(): Point = up().left()

  fun upRight(): Point = up().right()

  fun downLeft(): Point = down().left()

  fun downRight(): Point = down().right()

  companion object {
    private val X_DIRECTION = Point(1, 0)
    private val Y_DIRECTION = Point(0, 1)

    fun parse(pointStr: String): Point {
      val (x, y) = pointStr.split(",").map { it.toInt() }
      return Point(x, y)
    }
  }

  override fun compareTo(other: Point): Int = compareValuesBy(this, other, { it.x }, { it.y })
}

data class Line(val start: Point, val end: Point) {
  init {
    require(start.x == end.x || start.y == end.y) { "Line must be horizontal or vertical" }
  }

  private val xRange: IntRange = if (start.x < end.x) start.x..end.x else end.x..start.x

  private val yRange: IntRange = if (start.y < end.y) start.y..end.y else end.y..start.y

  fun expand(): List<Point> = xRange.flatMap { x -> yRange.map { y -> Point(x, y) } }

  companion object {
    fun parse(lineStr: String): Line {
      val (start, end) = lineStr.split(" -> ").map(Point.Companion::parse)
      return Line(start, end)
    }
  }
}

data class Path(val points: List<Point>) {
  fun expand(): List<Point> = points.zipWithNext(::Line).flatMap(Line::expand)

  companion object {
    fun parse(pathStr: String): Path {
      val points = pathStr.split(" -> ").map(Point.Companion::parse)
      return Path(points)
    }
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

  fun findAll(predicate: (T) -> Boolean): List<T> =
    grid.flatMap { row ->
      row.mapNotNull { col ->
        if (predicate(col)) col else null
      }
    }

  fun findAll(element: T): List<T> = findAll { it == element }

  fun find(predicate: (T) -> Boolean): T = findAll(predicate).first()

  fun find(element: T): T = find { it == element }

  fun indicesOf(predicate: (T) -> Boolean): List<Point> =
    grid.flatMapIndexed { y, row ->
      row.mapIndexedNotNull { x, col ->
        if (predicate(col)) Point(x, y) else null
      }
    }

  fun indicesOf(element: T): List<Point> = indicesOf { it == element }

  fun indexOf(predicate: (T) -> Boolean): Point = indicesOf(predicate).first()

  fun indexOf(element: T): Point = indexOf { it == element }

  fun zip(other: Grid<T>, transform: (T, T) -> T): Grid<T> = Grid(
    grid.zip(other.grid) { x, y -> x.zip(y, transform) }
  )

  fun count(predicate: (T) -> Boolean): Int = grid.sumOf { it.count(predicate) }

  operator fun get(point: Point): T = grid[point.y][point.x]

  operator fun contains(point: Point): Boolean =
    (point.x in (0 until width)) && (point.y in (0 until height))

  fun adjacents(point: Point, adjacency: Adjacency = Adjacency.ORTHOGONAL): List<Point> =
    point.adjacents(adjacency).filter { it in this }

  companion object {
    fun <T : Comparable<T>> Grid<T>.max(): T = grid.maxOf { it.max() }

    fun <T : Comparable<T>> Grid<T>.min(): T = grid.minOf { it.min() }

    fun parse(gridStr: String): Grid<Char> = Grid(gridStr.lines().map { it.toCharArray().toList() })
  }
}