package lib

import lib.ParserCombinators.many1
import lib.ParserCombinators.map
import lib.Tuples.to

typealias ParserState = String

data class ParserResult<out T>(val value: T, val nextState: ParserState)

fun interface Parser<out T> {
  fun parse(state: ParserState): ParserResult<T>?
}

object Parsers {
  fun <T> pure(value: T): Parser<T> = Parser { state -> ParserResult(value, state) }

  val fail: Parser<Nothing> = Parser { null }

  /**
   * Parses a parser recursively. This is useful for parsing recursive structures.
   * @param parser A function that returns a parser.
   */
  fun <T> recursiveParser(parser: () -> Parser<T>): Parser<T> = Parser { state ->
    parser().parse(state)
  }

  val eof: Parser<Unit> = Parser { state ->
    if (state.isEmpty()) {
      ParserResult(Unit, state)
    } else {
      null
    }
  }

  fun satisfy(predicate: (Char) -> Boolean): Parser<Char> = Parser { state ->
    if (state.isNotEmpty() && predicate(state.first())) {
      ParserResult(state.first(), state.drop(1))
    } else {
      null
    }
  }

  fun anyChar(): Parser<Char> = satisfy { true }

  fun char(c: Char): Parser<Char> = satisfy { it == c }

  fun exceptChar(c: Char): Parser<Char> = satisfy { it != c }

  fun anyOfString(str: String): Parser<Char> = satisfy { it in str }

  fun noneOfString(str: String): Parser<Char> = satisfy { it !in str }

  fun regexChar(regex: Regex): Parser<Char> = satisfy { it.toString().matches(regex) }

  fun regexChar(regex: String): Parser<Char> = regexChar(regex.toRegex())

  fun string(str: String): Parser<String> = Parser { state ->
    if (state.startsWith(str)) {
      ParserResult(str, state.drop(str.length))
    } else {
      null
    }
  }

  fun regex(regex: Regex): Parser<String> = Parser { state ->
    val matchResult = regex.find(state)
    if (matchResult != null && matchResult.range.first == 0) {
      val match = matchResult.value
      ParserResult(match, state.drop(match.length))
    } else {
      null
    }
  }

  val digit: Parser<Char> = satisfy { it.isDigit() }

  val digits: Parser<String> = digit.many1().map { it.joinToString("") }

  val letter: Parser<Char> = satisfy { it.isLetter() }

  val letters: Parser<String> = letter.many1().map { it.joinToString("") }

  val int: Parser<Int> = digits map { it.toInt() }

  val long: Parser<Long> = digits map { it.toLong() }

  val whitespace: Parser<Char> = satisfy { it.isWhitespace() }

  val whitespaces: Parser<String> = whitespace.many1().map { it.joinToString("") }

  val newline: Parser<Char> = char('\n')
}

object ParserCombinators {
  infix fun <T, R> Parser<T>.map(transform: (T) -> R): Parser<R> = Parser { state ->
    parse(state)?.let { result ->
      ParserResult(transform(result.value), result.nextState)
    }
  }

  infix fun <T, R> Parser<T>.chain(transform: (T) -> Parser<R>): Parser<R> = Parser { state ->
    parse(state)?.let { result ->
      transform(result.value).parse(result.nextState)
    }
  }

  infix fun <T1, T2> Parser<T1>.and(other: Parser<T2>): Parser<Pair<T1, T2>> = Parser { state ->
    parse(state)?.let { result ->
      other.parse(result.nextState)?.let { otherResult ->
        ParserResult(result.value to otherResult.value, otherResult.nextState)
      }
    }
  }

  infix fun <T> Parser<T>.or(other: Parser<T>): Parser<T> = Parser { state ->
    parse(state) ?: other.parse(state)
  }

  fun <T> choiceOf(parsers: List<Parser<T>>): Parser<T> = Parser { state ->
    parsers.firstNotNullOfOrNull { it.parse(state) }
  }

  fun <T> choiceOf(vararg parsers: Parser<T>): Parser<T> = choiceOf(parsers.toList())

  fun <T1, T2> sequenceOf(
    p1: Parser<T1>,
    p2: Parser<T2>,
  ): Parser<Pair<T1, T2>> =
    p1 and p2 map { it.first to it.second }

  fun <T1, T2, T3> sequenceOf(
    p1: Parser<T1>,
    p2: Parser<T2>,
    p3: Parser<T3>,
  ): Parser<Triple<T1, T2, T3>> = p1 and p2 and p3 map {
    it.first to it.second
  }

  fun <T1, T2, T3, T4> sequenceOf(
    p1: Parser<T1>,
    p2: Parser<T2>,
    p3: Parser<T3>,
    p4: Parser<T4>,
  ): Parser<Quadruple<T1, T2, T3, T4>> = (p1 and p2) and (p3 and p4) map {
    it.first to it.second
  }

  fun <T1, T2, T3, T4, T5> sequenceOf(
    p1: Parser<T1>,
    p2: Parser<T2>,
    p3: Parser<T3>,
    p4: Parser<T4>,
    p5: Parser<T5>,
  ): Parser<Quintuple<T1, T2, T3, T4, T5>> = (p1 and p2) and (p3 and p4 and p5) map {
    it.first to it.second.first to it.second.second
  }

  fun <T> Parser<T>.optional(): Parser<T?> = Parser { state ->
    parse(state)?.let { result ->
      ParserResult(result.value, result.nextState)
    } ?: ParserResult(null, state)
  }

  infix fun <T> Parser<T>.filter(predicate: (T) -> Boolean): Parser<T> = Parser { state ->
    parse(state)?.let { result ->
      if (predicate(result.value)) {
        ParserResult(result.value, result.nextState)
      } else {
        null
      }
    }
  }

  infix fun <T1, T2> Parser<T1>.takeRight(other: Parser<T2>): Parser<T2> = Parser { state ->
    parse(state)?.let { result ->
      other.parse(result.nextState)?.let { otherResult ->
        ParserResult(otherResult.value, otherResult.nextState)
      }
    }
  }

  infix fun <T1, T2> Parser<T1>.takeLeft(other: Parser<T2>): Parser<T1> = Parser { state ->
    parse(state)?.let { result ->
      other.parse(result.nextState)?.let { otherResult ->
        ParserResult(result.value, otherResult.nextState)
      }
    }
  }

  infix fun <T1, T2, T3> Parser<T1>.between(others: Pair<Parser<T2>, Parser<T3>>): Parser<T1> =
    others.first takeRight this takeLeft others.second

  fun <T> Parser<T>.many(): Parser<List<T>> = Parser { state ->
    val results = mutableListOf<T>()
    var currentState = state
    while (true) {
      val result = parse(currentState) ?: break
      results.add(result.value)
      currentState = result.nextState
    }
    ParserResult(results, currentState)
  }

  fun <T> Parser<T>.many1(): Parser<List<T>> =
    this and many() map { (first, rest) -> listOf(first) + rest }

  infix fun <T1, T2> Parser<T1>.manyTill(other: Parser<T2>): Parser<List<T1>> = Parser { state ->
    val results = mutableListOf<T1>()
    var currentState = state
    while (true) {
      if (other.parse(currentState) != null) {
        break
      }
      val result = parse(currentState) ?: break
      results.add(result.value)
      currentState = result.nextState
    }
    ParserResult(results, currentState)
  }

  infix fun <T1, T2> Parser<T1>.many1Till(other: Parser<T2>): Parser<List<T1>> =
    this and manyTill(other) map { (first, rest) -> listOf(first) + rest }

  infix fun <T> Parser<T>.count(count: Int): Parser<List<T>> = Parser { state ->
    val results = mutableListOf<T>()
    var currentState = state
    repeat(count) {
      val result = parse(currentState) ?: return@Parser null
      results.add(result.value)
      currentState = result.nextState
    }
    ParserResult(results, currentState)
  }

  infix fun <T1, T2> Parser<T1>.sepBy(sep: Parser<T2>): Parser<List<T1>> = Parser { state ->
    val results = mutableListOf<T1>()
    var currentState = state
    while (true) {
      val result = parse(currentState) ?: break
      results.add(result.value)
      currentState = result.nextState

      if (sep.parse(currentState) == null) {
        break
      }
      currentState = sep.parse(currentState)?.nextState ?: break
    }
    ParserResult(results, currentState)
  }

  infix fun <T1, T2> Parser<T1>.sepBy1(sep: Parser<T2>): Parser<List<T1>> =
    this takeLeft sep and sepBy(sep) map { (first, rest) -> listOf(first) + rest }
}
