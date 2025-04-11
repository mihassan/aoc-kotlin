package lib

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D) {
  override fun toString(): String = "($first, $second, $third, $fourth)"
}

data class Quintuple<A, B, C, D, E>(
  val first: A,
  val second: B,
  val third: C,
  val fourth: D,
  val fifth: E,
) {
  override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}
