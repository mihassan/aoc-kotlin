package lib

object DP {
  inline fun <I, O> memoize(crossinline fn: (I) -> O): ((I) -> O) {
    val m = mutableMapOf<I, O>()
    return { i -> m[i] ?: fn(i).also { o -> m[i] = o } }
  }
}