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

object Tuples {
  infix fun <P, Q, R> Pair<P, Q>.to(third: R): Triple<P, Q, R> = Triple(first, second, third)
  infix fun <P, Q, R> P.to(r: Pair<Q, R>): Triple<P, Q, R> = Triple(this, r.first, r.second)

  infix fun <P, Q, R, S> Triple<P, Q, R>.to(fourth: S): Quadruple<P, Q, R, S> =
    Quadruple(first, second, third, fourth)

  infix fun <P, Q, R, S> P.to(r: Triple<Q, R, S>): Quadruple<P, Q, R, S> =
    Quadruple(this, r.first, r.second, r.third)

  infix fun <P, Q, R, S> Pair<P, Q>.to(r: Pair<R, S>): Quadruple<P, Q, R, S> =
    Quadruple(first, second, r.first, r.second)

  infix fun <P, Q, R, S, T> Quadruple<P, Q, R, S>.to(fifth: T): Quintuple<P, Q, R, S, T> =
    Quintuple(first, second, third, fourth, fifth)

  infix fun <P, Q, R, S, T> P.to(r: Quadruple<Q, R, S, T>): Quintuple<P, Q, R, S, T> =
    Quintuple(this, r.first, r.second, r.third, r.fourth)

  infix fun <P, Q, R, S, T> Pair<P, Q>.to(r: Triple<R, S, T>): Quintuple<P, Q, R, S, T> =
    Quintuple(first, second, r.first, r.second, r.third)

  infix fun <P, Q, R, S, T> Triple<P, Q, R>.to(r: Pair<S, T>): Quintuple<P, Q, R, S, T> =
    Quintuple(first, second, third, r.first, r.second)
}