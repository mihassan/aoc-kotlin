@file:Suppress("PackageDirectoryMismatch")

package aoc21.day16

import lib.Parsers.anyOf
import lib.ParserCombinators.map
import lib.Parser
import lib.ParserCombinators.and
import lib.ParserResult
import lib.Solution
import lib.ParserCombinators.many
import lib.ParserCombinators.count
import lib.ParserCombinators.filter
import lib.ParserCombinators.flatMap
import lib.ParserCombinators.or
import lib.ParserCombinators.skipAnd
import lib.ParserCombinators.zip
import lib.Parsers.char

private sealed class Packet(open val version: Long) {
  abstract fun totalVersion(): Long

  abstract fun calculateValue(): Long

  data class Literal(override val version: Long, val value: Long) :
    Packet(version) {
    override fun totalVersion(): Long = version

    override fun calculateValue(): Long = value
  }

  sealed class Operator(override val version: Long, open val subPackets: List<Packet>) :
    Packet(version) {
    override fun totalVersion(): Long = version + subPackets.sumOf { it.totalVersion() }

    companion object {
      fun from(typeId: Long, version: Long, subPackets: List<Packet>): Operator {
        return when (typeId) {
          0L -> Sum(version, subPackets)
          1L -> Product(version, subPackets)
          2L -> Minimum(version, subPackets)
          3L -> Maximum(version, subPackets)
          5L -> GreaterThan(version, subPackets)
          6L -> LessThan(version, subPackets)
          7L -> EqualTo(version, subPackets)
          else -> error("Unknown operator type ID: $typeId")
        }
      }
    }
  }

  data class Sum(override val version: Long, override val subPackets: List<Packet>) :
    Operator(version, subPackets) {
    override fun calculateValue(): Long = subPackets.sumOf { it.calculateValue() }
  }

  data class Product(override val version: Long, override val subPackets: List<Packet>) :
    Operator(version, subPackets) {
    override fun calculateValue(): Long =
      subPackets.fold(1L) { acc, packet -> acc * packet.calculateValue() }
  }

  data class Minimum(override val version: Long, override val subPackets: List<Packet>) :
    Operator(version, subPackets) {
    override fun calculateValue(): Long = subPackets.minOf { it.calculateValue() }
  }

  data class Maximum(override val version: Long, override val subPackets: List<Packet>) :
    Operator(version, subPackets) {
    override fun calculateValue(): Long = subPackets.maxOf { it.calculateValue() }
  }

  data class GreaterThan(override val version: Long, override val subPackets: List<Packet>) :
    Operator(version, subPackets) {
    init {
      require(subPackets.size == 2) { "GreaterThan operator must have exactly 2 sub-packets" }
    }

    override fun calculateValue(): Long =
      if (subPackets[0].calculateValue() > subPackets[1].calculateValue()) 1 else 0
  }

  data class LessThan(override val version: Long, override val subPackets: List<Packet>) :
    Operator(version, subPackets) {
    init {
      require(subPackets.size == 2) { "LessThan operator must have exactly 2 sub-packets" }
    }

    override fun calculateValue(): Long =
      if (subPackets[0].calculateValue() < subPackets[1].calculateValue()) 1 else 0
  }

  data class EqualTo(override val version: Long, override val subPackets: List<Packet>) :
    Operator(version, subPackets) {
    init {
      require(subPackets.size == 2) { "EqualTo operator must have exactly 2 sub-packets" }
    }

    override fun calculateValue(): Long =
      if (subPackets[0].calculateValue() == subPackets[1].calculateValue()) 1 else 0
  }
}

private fun bitsParser(n: Int): Parser<Long> =
  anyOf("01").count(n).map { it.joinToString("") }.map { it.toLong(2) }

private val versionParser: Parser<Long> = bitsParser(3)
private val typeIdParser: Parser<Long> = bitsParser(3)

private fun chunkParser(indicator: Char): Parser<String> =
  char(indicator) skipAnd anyOf("01").count(4) map { it.joinToString("") }

private val terminalChunkParser: Parser<String> = chunkParser('0')
private val initialChunkParser: Parser<String> = chunkParser('1')
private val initialChunksParser: Parser<String> =
  initialChunkParser.many().map { it.joinToString("") }

private val literalValueParser: Parser<Long> =
  (initialChunksParser and terminalChunkParser).map {
    (it.first + it.second).toLong(2)
  }

private val literalPacketParser: Parser<Packet.Literal> =
  zip(versionParser, typeIdParser, literalValueParser)
    .filter { (_, typeId, _) -> typeId == 4L }
    .map { (version, _, value) -> Packet.Literal(version, value) }

private val subPacketsByLengthParser: Parser<List<Packet>> =
  (char('0') skipAnd bitsParser(15)).flatMap { length ->
    Parser { state ->
      val subPackets = packetParser.many().parse(state.take(length.toInt()))?.value ?: emptyList()
      val nextState = state.drop(length.toInt())
      ParserResult(subPackets, nextState)
    }
  }

private val subPacketsByCountParser: Parser<List<Packet>> =
  (char('1') skipAnd bitsParser(11)).flatMap { count ->
    packetParser.count(count.toInt())
  }

private val subPacketsParser: Parser<List<Packet>> =
  subPacketsByLengthParser or subPacketsByCountParser

private val operatorPacketParser: Parser<Packet.Operator> =
  zip(versionParser, typeIdParser, subPacketsParser)
    .filter { (_, typeId, _) -> typeId != 4L }
    .map { (version, typeId, subPackets) -> Packet.Operator.from(typeId, version, subPackets) }

private val packetParser: Parser<Packet> = literalPacketParser or operatorPacketParser

private typealias Input = String
private typealias Output = Long

private val solution = object : Solution<Input, Output>(2021, "Day16") {
  override fun parse(input: String): Input =
    input.map { it.digitToInt(16).toString(2).padStart(4, '0') }.joinToString("")

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    val (packet, _) = packetParser.parse(input) ?: return 0
    return packet.totalVersion()
  }

  override fun part2(input: Input): Output {
    val (packet, _) = packetParser.parse(input) ?: return 0
    return packet.calculateValue()
  }
}

fun main() = solution.run()
