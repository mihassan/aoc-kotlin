package lib

object Functional {
  fun <R> Boolean.whenTrue(block: () -> Unit): Unit = if (this) block() else Unit
  fun <R> Boolean.whenFalse(block: () -> Unit): Unit = if (!this) block() else Unit

  infix fun <P, Q, R> Pair<P, Q>.to(third: R): Triple<P, Q, R> = Triple(first, second, third)
  infix fun <P, Q, R> P.to(r: Pair<Q, R>): Triple<P, Q, R> = Triple(this, r.first, r.second)
}
