@file:Suppress("PackageDirectoryMismatch")

package aoc23.day03

import aoc23.day03.PartNumber.Companion.findAllPartNumbers
import aoc23.day03.Symbol.Companion.findAllSymbols
import lib.Line
import lib.Point
import lib.Solution

data class PartNumber(val value: Int, val location: Line) {
  companion object {
    fun String.findAllPartNumbers(): List<PartNumber> =
      lines().flatMapIndexed { i, line ->
        Regex("""\d+""").findAll(line).map { it.toPartNumber(i) }
      }

    private fun MatchResult.toPartNumber(rowIdx: Int) =
      PartNumber(value.toInt(), Line(Point(range.first, rowIdx), Point(range.last, rowIdx)))
  }
}

data class Symbol(val value: Char, val location: Point) {
  fun isStar() = value == '*'

  companion object {
    fun String.findAllSymbols(): List<Symbol> =
      lines().flatMapIndexed { i, line ->
        Regex("""[^\.\d]""").findAll(line).map { it.toSymbol(i) }
      }

    private fun MatchResult.toSymbol(rowIdx: Int) =
      Symbol(value.single(), Point(range.first, rowIdx))
  }
}

data class Gear(val symbol: Symbol, val partNumbers: List<PartNumber>) {
  constructor(mapEntry: Map.Entry<Symbol, List<PartNumber>>) : this(mapEntry.key, mapEntry.value)

  fun value(): Int = partNumbers.map { it.value }.reduce(Int::times)

  fun isValidGear() = partNumbers.size == 2
}

data class EngineSchematic(val partNumbers: List<PartNumber>, val symbols: List<Symbol>) {
  fun extractValidPartNumbers(): List<PartNumber> = partNumbers.filter(::isValidPartNumber)

  fun extractGears(): List<Gear> = symbols
    .filter(Symbol::isStar)
    .associateWith(::adjacentPartNumbers)
    .map(::Gear)
    .filter(Gear::isValidGear)

  private fun isValidPartNumber(partNumber: PartNumber): Boolean =
    symbols.any { symbol ->
      partNumber isAdjacentTo symbol
    }

  private fun adjacentPartNumbers(symbol: Symbol): List<PartNumber> =
    partNumbers.filter { partNumber ->
      partNumber isAdjacentTo symbol
    }

  private infix fun PartNumber.isAdjacentTo(symbol: Symbol): Boolean {
    val xRange = location.start.x - 1..location.end.x + 1
    val yRange = location.start.y - 1..location.end.y + 1
    return symbol.location.x in xRange && symbol.location.y in yRange
  }

  companion object {
    fun parse(schematicStr: String): EngineSchematic =
      EngineSchematic(schematicStr.findAllPartNumbers(), schematicStr.findAllSymbols())
  }
}

typealias Input = EngineSchematic

typealias Output = Int

private val solution = object : Solution<Input, Output>(2023, "Day03") {
  override fun parse(input: String): Input = EngineSchematic.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.extractValidPartNumbers().sumOf(PartNumber::value)

  override fun part2(input: Input): Output =
    input.extractGears().sumOf(Gear::value)
}

fun main() = solution.run()
