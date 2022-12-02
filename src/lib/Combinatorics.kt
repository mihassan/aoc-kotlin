package lib

object Combinatorics {
  fun <T> permutationsWithReplacement(input: Set<T>, n: Int): List<List<T>> =
    if (n <= 0) listOf(emptyList())
    else input.flatMap { first ->
      permutationsWithReplacement(input, n - 1).map { rest ->
        listOf(first) + rest
      }
    }

  fun <T> permutations(input: Set<T>): List<List<T>> {
    if (input.isEmpty()) return listOf(emptyList())

    val first = input.first()
    val tail = input.drop(1).toSet()

    return permutations(tail).flatMap { rest ->
      (0..rest.size).map {
        buildList {
          addAll(rest.take(it))
          add(first)
          addAll(rest.drop(it))
        }
      }
    }
  }

  fun <T> combinations(input: Set<T>, n: Int): Set<Set<T>> {
    if (n <= 0) return setOf(emptySet())
    if (input.isEmpty()) return emptySet()

    val first = input.take(1).toSet()
    val tail = input.drop(1).toSet()

    return combinations(tail, n - 1).map { rest -> first + rest }.toSet() + combinations(tail, n)
  }
}
