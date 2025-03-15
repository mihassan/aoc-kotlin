package lib

object DP {
  inline fun <I, O> memoize(crossinline fn: (I) -> O): ((I) -> O) {
    val m = mutableMapOf<I, O>()
    return { i -> m[i] ?: fn(i).also { o -> m[i] = o } }
  }

  inline fun <I1, I2, O> memoize(crossinline fn: (I1, I2) -> O): ((I1, I2) -> O) {
    val m = mutableMapOf<Pair<I1, I2>, O>()
    return { i1, i2 -> m[i1 to i2] ?: fn(i1, i2).also { o -> m[i1 to i2] = o } }
  }
}