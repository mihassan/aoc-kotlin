package lib

/**
 * A type alias for the parser state, which is represented as a string.
 * This is the input string that the parser will process.
 */
typealias ParserState = String

/**
 * A data class representing the result of a parser.
 * It contains the parsed result and the remaining state after parsing.
 *
 * @param T The type of the parsed result.
 * @param result The parsed result.
 * @param state The remaining state after parsing.
 */
data class ParserResult<T>(val result: T, val state: ParserState)

/**
 * A type alias for a parser function.
 * It takes the current parser state and returns a nullable ParserResult.
 *
 * @param T The type of the parsed result.
 */
typealias Parser<T> = (ParserState) -> ParserResult<T>?

/**
 * A parser that consumes a character from the input string that satisfies the given predicate.
 */
fun satisfy(predicate: (Char) -> Boolean): Parser<Char> =
  { state ->
    if (state.isNotEmpty() && predicate(state[0])) {
      ParserResult(state[0], state.substring(1))
    } else {
      null
    }
  }

/**
 * A parser that consumes a single character from the input string.
 */
fun char(char: Char): Parser<Char> = satisfy { it == char }

/**
 * A parser that checks if all input has been consumed.
 */
fun eof(): Parser<Unit> = { state ->
  if (state.isEmpty()) {
    ParserResult(Unit, state)
  } else {
    null
  }
}

/**
 * A parser combinator that applies the first parser and, if it fails, applies the second parser.
 */
infix fun <T> Parser<T>.or(other: Parser<T>): Parser<T> = { state -> this(state) ?: other(state) }

/**
 * A parser combinator that applies the first parser and, if it succeeds, applies the second parser.
 * The result is a pair of the results from both parsers.
 */
infix fun <T1, T2> Parser<T1>.and(other: Parser<T2>): Parser<Pair<T1, T2>> = { state ->
  this(state)?.let { result1 ->
    other(result1.state)?.let { result2 ->
      ParserResult(Pair(result1.result, result2.result), result2.state)
    }
  }
}

/**
 * A parser combinator that applies the first parser and transforms its result.
 */
infix fun <T, R> Parser<T>.map(transform: (T) -> R): Parser<R> = { state ->
  this(state)?.let { result ->
    ParserResult(transform(result.result), result.state)
  }
}

/**
 * A parser combinator that applies the first parser and, if it succeeds, applies the second parser
 * to the result of the first parser. In contrast to `map`, this allows for chaining parsers
 * where the second parser depends on the result of the first.
 */
infix fun <T, R> Parser<T>.flatMap(transform: (T) -> Parser<R>): Parser<R> = { state ->
  this(state)?.let { result ->
    transform(result.result)(result.state)
  }
}

/**
 * A parser combinator tries to apply the parser and, if it fails, returns null.
 * The parser never fails.
 */
fun <T> Parser<T>.optional(): Parser<T?> = { state ->
  this(state)?.let { result ->
    ParserResult(result.result, result.state)
  } ?: ParserResult(null, state)
}

/**
 * A parser combinator that applies the first parser provided it is followed by the second parser.
 */
infix fun <T1, T2> Parser<T1>.followedBy(other: Parser<T2>): Parser<T1> = { state ->
  this(state)?.let { result1 ->
    other(result1.state)?.let { result2 ->
      ParserResult(result1.result, result2.state)
    }
  }
}

/**
 * A parser combinator that applies the first parser and ignores its result,
 * returning the result of the second parser.
 */
infix fun <T1, T2> Parser<T1>.skip(other: Parser<T2>): Parser<T2> = { state ->
  this(state)?.let { result1 ->
    other(result1.state)?.let { result2 ->
      ParserResult(result2.result, result2.state)
    }
  }
}

/**
 * A parser combinator that applies the first parser as many times as possible,
 * returning a list of results.
 */
fun <T> Parser<T>.many(): Parser<List<T>> = { state ->
  val results = mutableListOf<T>()
  var currentState = state
  while (true) {
    this(currentState)?.let { result ->
      results.add(result.result)
      currentState = result.state
    } ?: break
  }
  ParserResult(results, currentState)
}

/**
 * A parser combinator that applies the first parser at least once,
 * returning a list of results.
 */
fun <T> Parser<T>.many1(): Parser<List<T>> = { state ->
  this.many()(state)?.let { result ->
    if (result.result.isNotEmpty()) {
      ParserResult(result.result, result.state)
    } else {
      null
    }
  }
}

/**
 * Runs the parser on the input string and returns the result.
 * The remaining input is discarded if the parser succeeds.
 */
fun <T> Parser<T>.parse(input: String): T? =
  this(input)?.let { result -> result.result }

/**
 * Runs the parser on the entire input string and returns the result.
 * If the parser does not consume the entire input, it returns null.
 */
fun <T> Parser<T>.parseFull(input: String): T? = (this followedBy eof()).parse(input)

fun main() {
  // Example usage of the parser combinator library
  val parser: Parser<String> =
    char('(') skip (satisfy { it.isLetter() }.many().map { it.joinToString("") }) followedBy char(')')
  val result = parser.parse("(abcd)")
  println(result) // Output: "abcd"

  val parser2: Parser<Int> =
    char('-').optional() and satisfy { it.isDigit() }.many1().map { it.joinToString("") } map {
      if(it.first == '-') {
        -it.second.toInt()
      } else {
        it.second.toInt()
      }
    }
  val result2 = parser2.parseFull("-123")
  println(result2) // Output: -123

  data class Point(val x: Int, val y: Int)

  fun int(): Parser<Int> =
    satisfy { it.isDigit() }.many1() map { it.joinToString("").toInt() }

  val parser3: Parser<Point> =
    int() followedBy char(',') and int() map { (x, y) -> Point(x, y) }
  val result3 = parser3.parseFull("123,456")
  println(result3) // Output: Point(x=123, y=456)
}
