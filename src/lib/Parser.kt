package lib

/**
 * A type alias for the parser nextState, which is represented as a string.
 * This is the input string that the parser will process.
 */
typealias ParserState = String

/**
 * A data class representing the value of a parser.
 * It contains the parsed value and the remaining nextState after parsing.
 *
 * @param T The type of the parsed value.
 * @param value The parsed value.
 * @param nextState The remaining nextState after parsing.
 */
data class ParserResult<T>(val value: T, val nextState: ParserState)

/**
 * A functional interface representing a parser.
 * It takes the current parser state and returns a nullable ParserResult.
 * If parsing fails, it returns null.
 *
 * @param T The type of the parsed value.
 */
fun interface Parser<T> {
  fun parse(state: ParserState): ParserResult<T>?
}

/**
 * A parser that checks if all input has been consumed.
 */
fun eof(): Parser<Unit> = Parser { state ->
  if (state.isEmpty()) {
    ParserResult(Unit, state)
  } else {
    null
  }
}

/**
 * A parser that consumes a character from the input string that satisfies the given predicate.
 */
fun satisfy(predicate: (Char) -> Boolean): Parser<Char> = Parser { state ->
  if (state.isNotEmpty() && predicate(state[0])) {
    ParserResult(state[0], state.substring(1))
  } else {
    null
  }
}

/**
 * A parser that consumes any character from the input string.
 */
fun anyChar(): Parser<Char> = satisfy { true }

/**
 * A parser that consumes a single character from the input string.
 */
fun char(char: Char): Parser<Char> = satisfy { it == char }

/**
 * A parser that consumes a string from the input string.
 */
fun string(str: String): Parser<String> = Parser { state ->
  if (state.startsWith(str)) {
    ParserResult(str, state.substring(str.length))
  } else {
    null
  }
}

/**
 * A parser combinator that applies the first parser and, if it fails, applies the second parser.
 */
infix fun <T> Parser<T>.or(other: Parser<T>): Parser<T> =
  Parser { state -> parse(state) ?: other.parse(state) }

/**
 * A parser combinator that applies the first parser and, if it succeeds, applies the second parser.
 * The value is a pair of the results from both parsers.
 */
infix fun <T1, T2> Parser<T1>.and(other: Parser<T2>): Parser<Pair<T1, T2>> =
  Parser { state ->
    parse(state)?.let { result1 ->
      other.parse(result1.nextState)?.let { result2 ->
        ParserResult(Pair(result1.value, result2.value), result2.nextState)
      }
    }
  }

/**
 * A parser combinator that matches any of the given parsers.
 */
fun <T> anyOf(vararg parsers: Parser<T>): Parser<T> = Parser { state ->
  parsers.firstNotNullOfOrNull { it.parse(state) }?.let { result ->
    ParserResult(result.value, result.nextState)
  }
}

/**
 * A parser combinator that applies all the given parsers in sequence.
 */
fun <T> allOf(vararg parsers: Parser<T>): Parser<List<T>> = Parser { state ->
  val results = mutableListOf<T>()
  var currentState = state
  for (parser in parsers) {
    parser.parse(currentState)?.let { result ->
      results.add(result.value)
      currentState = result.nextState
    } ?: return@Parser null
  }
  ParserResult(results, currentState)
}

/**
 * A family of parser combinators that applies multiple parsers in sequence.
 * The parsers can have different types.
 * The result is a tuple of the results from each parser.
 */
fun <T1, T2> Parser<T1>.zip(other: Parser<T2>): Parser<Pair<T1, T2>> =
  this and other

fun <T1, T2, T3> Parser<T1>.zip(
  parser2: Parser<T2>,
  parser3: Parser<T3>,
): Parser<Triple<T1, T2, T3>> =
  (this and parser2 and parser3).map {
    Triple(it.first.first, it.first.second, it.second)
  }

fun <T1, T2, T3, T4> Parser<T1>.zip(
  parser2: Parser<T2>,
  parser3: Parser<T3>,
  parser4: Parser<T4>,
): Parser<Quadruple<T1, T2, T3, T4>> =
  (this and parser2 and parser3 and parser4).map {
    Quadruple(it.first.first.first, it.first.first.second, it.first.second, it.second)
  }

fun <T1, T2, T3, T4, T5> Parser<T1>.zip(
  parser2: Parser<T2>,
  parser3: Parser<T3>,
  parser4: Parser<T4>,
  parser5: Parser<T5>,
): Parser<Quintuple<T1, T2, T3, T4, T5>> =
  (this and parser2 and parser3 and parser4 and parser5).map {
    Quintuple(
      it.first.first.first.first,
      it.first.first.first.second,
      it.first.first.second,
      it.first.second,
      it.second,
    )
  }

/**
 * A parser combinator that applies the first parser and transforms its value.
 */
infix fun <T, R> Parser<T>.map(transform: (T) -> R): Parser<R> = Parser { state ->
  parse(state)?.let { result ->
    ParserResult(transform(result.value), result.nextState)
  }
}

/**
 * A parser combinator that applies the first parser and, if it succeeds, applies the second parser
 * to the value of the first parser. In contrast to `map`, this allows for chaining parsers
 * where the second parser depends on the value of the first.
 */
infix fun <T, R> Parser<T>.flatMap(transform: (T) -> Parser<R>): Parser<R> = Parser { state ->
  parse(state)?.let { result ->
    val nextParser = transform(result.value)
    nextParser.parse(result.nextState)
  }
}

/**
 * A parser combinator tries to apply the parser and, if it fails, returns null.
 * The parser never fails.
 */
fun <T> Parser<T>.optional(): Parser<T?> = Parser { state ->
  parse(state)?.let { result ->
    ParserResult(result.value, result.nextState)
  } ?: ParserResult(null, state)
}

/**
 * A parser combinator that applies the first parser provided it is followed by the second parser.
 */
infix fun <T1, T2> Parser<T1>.skipSecond(other: Parser<T2>): Parser<T1> = Parser { state ->
  parse(state)?.let { result1 ->
    other.parse(result1.nextState)?.let { result2 ->
      ParserResult(result1.value, result2.nextState)
    }
  }
}

/**
 * A parser combinator that applies the first parser provided it is between two other parsers.
 */
infix fun <T1, T2, T3> Parser<T1>.between(
  parsers: Pair<Parser<T2>, Parser<T3>?>,
): Parser<T1> = when (parsers.second) {
  null -> parsers.first skipFirst this skipSecond parsers.first
  else -> parsers.first skipFirst this skipSecond parsers.second!!
}

/**
 * A parser combinator that applies the first parser and ignores its value,
 * returning the value of the second parser.
 */
infix fun <T1, T2> Parser<T1>.skipFirst(other: Parser<T2>): Parser<T2> = Parser { state ->
  parse(state)?.let { result1 ->
    other.parse(result1.nextState)?.let { result2 ->
      ParserResult(result2.value, result2.nextState)
    }
  }
}

/**
 * A parser combinator that applies the first parser as many times as possible,
 * returning a list of results.
 */
fun <T> Parser<T>.many(): Parser<List<T>> = Parser { state ->
  val results = mutableListOf<T>()
  var currentState = state
  while (true) {
    parse(currentState)?.let { result ->
      results.add(result.value)
      currentState = result.nextState
    } ?: break
  }
  ParserResult(results, currentState)
}

/**
 * A parser combinator that applies the first parser at least once,
 * returning a list of results.
 */
fun <T> Parser<T>.many1(): Parser<List<T>> = Parser { state ->
  parse(state)?.let { result ->
    val results = mutableListOf(result.value)
    var currentState = result.nextState
    while (true) {
      parse(currentState)?.let { result ->
        results.add(result.value)
        currentState = result.nextState
      } ?: break
    }
    ParserResult(results, currentState)
  }
}

/**
 * A parser combinator that applies the parser n times,
 */
fun <T> Parser<T>.count(n: Int): Parser<List<T>> = Parser { state ->
  val results = mutableListOf<T>()
  var currentState = state
  repeat(n) {
    parse(currentState)?.let { result ->
      results.add(result.value)
      currentState = result.nextState
    } ?: return@Parser null
  }
  ParserResult(results, currentState)
}

/**
 * Converts a parser that returns a list of characters into a parser that returns a string.
 */
fun Parser<List<Char>>.asStr(): Parser<String> = map { it.joinToString("") }

/**
 * Converts a parser that returns a string into a parser that returns an int.
 */
fun Parser<String>.asInt(): Parser<Int> = map { it.toInt() }

@JvmName("asIntList")
fun Parser<List<Char>>.asInt(): Parser<Int> = this.asStr().asInt()

fun main() {
  // Example usage of the parser combinator library
  val parser: Parser<String> =
    char('(') skipFirst (satisfy { it.isLetter() }.many().asStr()) skipSecond char(')')
  val result = parser.parse("(abcd)")
  println(result) // Output: "abcd"

  val parser2: Parser<Int> =
    char('-').optional() and satisfy { it.isDigit() }.many1().asStr() map {
      if (it.first == '-') {
        -it.second.toInt()
      } else {
        it.second.toInt()
      }
    }
  val result2 = parser2.parse("-123")
  println(result2) // Output: -123

  data class Point(val x: Int, val y: Int)

  fun int(): Parser<Int> =
    satisfy { it.isDigit() }.many1().asInt()

  val parser3: Parser<Point> =
    int() skipSecond char(',') and int() map { (x, y) -> Point(x, y) }
  val result3 = parser3.parse("123,456")
  println(result3) // Output: Point(x=123, y=456)

  val parser4: Parser<List<Int>> =
    int().many() between (char('(') to char(')')) skipSecond eof()
  val result4 = parser4.parse("(1,2,3)")
  println(result4) // Output: [1, 2, 3]

  data class Book(val title: String, val author: String, val year: Int)

  val parser4a: Parser<String> = satisfy { it != ',' }.many1().asStr() skipSecond string(", ")
  val parser5: Parser<Book> = parser4a.zip(parser4a, int())
    .map { Book(it.first, it.second, it.third) }

  val result5 = parser5.parse("The Catcher in the Rye, J.D. Salinger, 1951")
  println(result5) // Output: (The Catcher in the Rye, J.D. Salinger, 1951)
}
