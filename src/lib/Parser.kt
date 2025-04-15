package lib

typealias ParserState = String

data class ParserResult<out T>(val value: T, val nextState: ParserState)

fun interface Parser<out T> {
  fun parse(state: ParserState): ParserResult<T>?
}

object Parsers {
  fun <T> pure(value: T): Parser<T> = Parser { state ->
    ParserResult(value, state)
  }

  val fail: Parser<Unit> = Parser { null }

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

  fun char(c: Char): Parser<Char> = satisfy { it == c }

  fun anyChar(): Parser<Char> = satisfy { true }

  fun anyOf(chars: String): Parser<Char> = satisfy { it in chars }

  fun anyOf(regex: Regex): Parser<Char> = satisfy { it.toString().matches(regex) }

  fun noneOf(chars: String): Parser<Char> = satisfy { it !in chars }

  fun noneOf(regex: Regex): Parser<Char> = satisfy { it.toString().matches(regex).not() }

  fun string(str: String): Parser<String> = Parser { state ->
    if (state.startsWith(str)) {
      ParserResult(str, state.drop(str.length))
    } else {
      null
    }
  }

  fun string(regex: Regex): Parser<String> = Parser { state ->
    val matchResult = regex.find(state)
    if (matchResult != null &&  matchResult.range.first == 0) {
      val match = matchResult.value
      ParserResult(match, state.drop(match.length))
    } else {
      null
    }
  }
}

object ParserCombinators {
  infix fun <T, R> Parser<T>.map(transform: (T) -> R): Parser<R> = Parser { state ->
    parse(state)?.let { result ->
      ParserResult(transform(result.value), result.nextState)
    }
  }

  infix fun <T, R> Parser<T>.flatMap(transform: (T) -> Parser<R>): Parser<R> = Parser { state ->
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

  infix fun <T1, T2> Parser<T1>.skipAnd(other: Parser<T2>): Parser<T2> = Parser { state ->
    parse(state)?.let { result ->
      other.parse(result.nextState)?.let { otherResult ->
        ParserResult(otherResult.value, otherResult.nextState)
      }
    }
  }

  infix fun <T1, T2> Parser<T1>.andSkip(other: Parser<T2>): Parser<T1> = Parser { state ->
    parse(state)?.let { result ->
      other.parse(result.nextState)?.let { otherResult ->
        ParserResult(result.value, otherResult.nextState)
      }
    }
  }

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

  fun <T> Parser<T>.many1(): Parser<List<T>> = Parser { state ->
    val results = mutableListOf<T>()
    var currentState = state
    val firstResult = parse(currentState) ?: return@Parser null
    results.add(firstResult.value)
    currentState = firstResult.nextState

    while (true) {
      val result = parse(currentState) ?: break
      results.add(result.value)
      currentState = result.nextState
    }
    ParserResult(results, currentState)
  }

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

  infix fun <T1, T2> Parser<T1>.many1Till(other: Parser<T2>): Parser<List<T1>> = Parser { state ->
    val results = mutableListOf<T1>()
    var currentState = state
    if (other.parse(currentState) != null) return@Parser null
    val firstResult = parse(currentState) ?: return@Parser null
    results.add(firstResult.value)
    currentState = firstResult.nextState

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
}
