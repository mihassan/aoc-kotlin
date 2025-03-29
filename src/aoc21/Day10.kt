@file:Suppress("PackageDirectoryMismatch")

package aoc21.day10

import lib.Solution

private data class Symbol(val char: Char) {
  init {
    require(char in "({[<>]})") { "Invalid symbol: $char" }
  }

  fun illegalSymbolScore(): Long = when (char) {
    ')' -> 3
    ']' -> 57
    '}' -> 1197
    '>' -> 25137
    // The score for opening symbols is not required
    else -> 0
  }

  fun incompleteSymbolScore(): Long = when (char) {
    ')' -> 1
    ']' -> 2
    '}' -> 3
    '>' -> 4
    // The score for opening symbols is not required
    else -> 0
  }

  fun isOpening(): Boolean = char in "({[<"

  fun matchingSymbol(): Symbol = when (char) {
    '(' -> Symbol(')')
    '[' -> Symbol(']')
    '{' -> Symbol('}')
    '<' -> Symbol('>')
    ')' -> Symbol('(')
    ']' -> Symbol('[')
    '}' -> Symbol('{')
    '>' -> Symbol('<')
    else -> throw IllegalArgumentException("Invalid symbol: $char")
  }

  infix fun notMatching(other: Symbol): Boolean = matchingSymbol() != other
}

private data class Chunk(val symbols: List<Symbol>) {
  fun isCorrupt(): Boolean = findFirstCorruptSymbol() != null

  fun findFirstCorruptSymbol(): Symbol? {
    // We use a stack to keep track of the opening symbols.
    val stack = ArrayDeque<Symbol>()

    symbols.forEach { symbol ->
      when {
        // If the symbol is opening, we add it to the stack.
        symbol.isOpening() -> stack.addLast(symbol)
        // If the symbol is closing and does not match the last opening symbol, we return it.
        stack.isEmpty() -> return symbol
        // If the symbol is closing and does not match the last opening symbol, we return it.
        // Regardless of whether it matches or not, we remove the last opening symbol.
        stack.removeLast() notMatching symbol -> return symbol
      }
    }

    // If we reach here, it means all symbols are valid, even if the chunk is incomplete.
    return null
  }

  fun findCompletionSymbols(): Chunk {
    require(!isCorrupt()) { "Chunk is corrupt, so cannot find completion symbols." }

    // We use a stack to keep track of the opening symbols.
    val stack = ArrayDeque<Symbol>()

    symbols.forEach { symbol ->
      if (symbol.isOpening()) {
        stack.addLast(symbol)
      } else {
        stack.removeLast()
      }
    }

    // The stack now contains the opening symbols that need to be completed in reverse order.
    return Chunk(stack.map { it.matchingSymbol() }.reversed())
  }

  fun incompleteSymbolScore(): Long =
    symbols.fold(0L) { acc, symbol ->
      acc * 5 + symbol.incompleteSymbolScore()
    }

  companion object {
    fun parse(chunkStr: String): Chunk = Chunk(chunkStr.map(::Symbol))
  }
}

private typealias Input = List<Chunk>

private typealias Output = Long

private val solution = object : Solution<Input, Output>(2021, "Day10") {
  override fun parse(input: String): Input = input.lines().map(Chunk::parse)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input
      .mapNotNull { chunk -> chunk.findFirstCorruptSymbol() }
      .sumOf { symbol -> symbol.illegalSymbolScore() }

  override fun part2(input: Input): Output =
    input
      .filterNot(Chunk::isCorrupt)
      .map { chunk -> chunk.findCompletionSymbols() }
      .map { chunk -> chunk.incompleteSymbolScore() }
      .median()

  private fun <T : Comparable<T>> List<T>.median(): T = sorted()[size / 2]
}

fun main() = solution.run()
