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

private sealed class Packet(open val version: Long, open val typeId: Long) {
  abstract fun totalVersion(): Long

  abstract fun calculateValue(): Long

  data class Literal(override val version: Long, override val typeId: Long, val value: Long) :
    Packet(version, typeId) {
    override fun totalVersion(): Long = version

    override fun calculateValue(): Long = value
  }

  data class Operator(
    override val version: Long,
    override val typeId: Long,
    val subPackets: List<Packet>,
  ) :
    Packet(version, typeId) {
    override fun totalVersion(): Long = version + subPackets.sumOf { it.totalVersion() }

    override fun calculateValue(): Long = when (typeId) {
      0L -> subPackets.sumOf { it.calculateValue() }
      1L -> subPackets.fold(1L) { acc, packet -> acc * packet.calculateValue() }
      2L -> subPackets.minOf { it.calculateValue() }
      3L -> subPackets.maxOf { it.calculateValue() }
      5L -> if (subPackets[0].calculateValue() > subPackets[1].calculateValue()) 1 else 0
      6L -> if (subPackets[0].calculateValue() < subPackets[1].calculateValue()) 1 else 0
      7L -> if (subPackets[0].calculateValue() == subPackets[1].calculateValue()) 1 else 0
      else -> throw IllegalArgumentException("Unknown operator type: $typeId")
    }
  }
}

private typealias Input = String
private typealias Output = Long

// region Packet Parsers

/**
 * Parses a binary string into a long value.
 * The number of bits to parse is specified by the parameter [n].
 * For example, if [n] is 3, it will parse 3 bits and convert them to a long value.
 */
private fun bitsParser(n: Int): Parser<Long> =
  anyOf("01").count(n).map { it.joinToString("") }.map { it.toLong(2) }

/** Parses a 3-bit version number. */
private val versionParser: Parser<Long> = bitsParser(3)

/** Parses a 3-bit type ID. */
private val typeIdParser: Parser<Long> = bitsParser(3)

/**
 * Parses a chunk of binary data as part of the literal value.
 * The chunk starts with a specified [indicator] character, followed by 4 bits (0 or 1).
 * For example, if [indicator] is '0', it will parse '0' followed by 4 bits.
 */
private fun chunkParser(indicator: Char): Parser<String> =
  char(indicator) skipAnd anyOf("01").count(4) map { it.joinToString("") }

/**
 * Parses a terminal chunk of binary data for the literal value.
 * The terminal chunk starts with '0' followed by 4 bits (0 or 1).
 * For example, it will parse '0' followed by 4 bits.
 */
private val terminalChunkParser: Parser<String> = chunkParser('0')

/**
 * Parses the initial chunk of binary data for the literal value. There are multiple initial chunks.
 * The initial chunk starts with '1' followed by 4 bits (0 or 1).
 * For example, it will parse '1' followed by 4 bits.
 */
private val initialChunkParser: Parser<String> = chunkParser('1')

/**
 * Parses the initial chunks of binary data for the literal value.
 * It parses multiple initial chunks and combines them into a single string.
 */
private val initialChunksParser: Parser<String> =
  initialChunkParser.many().map { it.joinToString("") }

/**
 * Parses the literal value of a packet by combining the initial chunks and the terminal chunk.
 */
private val literalValueParser: Parser<Long> =
  (initialChunksParser and terminalChunkParser).map {
    (it.first + it.second).toLong(2)
  }

/**
 * Parses a literal packet by combining the version, type ID, and literal value.
 * It filters the parsed packet to ensure that the type ID is 4 (indicating a literal packet).
 */
private val literalPacketParser: Parser<Packet.Literal> =
  zip(versionParser, typeIdParser, literalValueParser)
    .filter { (_, typeId, _) -> typeId == 4L }
    .map { (version, typeId, value) -> Packet.Literal(version, typeId, value) }

/**
 * Parses the sub-packets of an operator packet given the length of bits to read.
 * The length is specified by 15 bits following the '0' indicator.
 */
private val subPacketsByLengthParser: Parser<List<Packet>> =
  (char('0') skipAnd bitsParser(15)).flatMap { length ->
    Parser { state ->
      val subPackets = packetParser.many().parse(state.take(length.toInt()))?.value ?: emptyList()
      val nextState = state.drop(length.toInt())
      ParserResult(subPackets, nextState)
    }
  }

/**
 * Parses the sub-packets of an operator packet given the number of sub-packets to read.
 * The number is specified by 11 bits following the '1' indicator.
 */
private val subPacketsByCountParser: Parser<List<Packet>> =
  (char('1') skipAnd bitsParser(11)).flatMap { count ->
    packetParser.count(count.toInt())
  }

/**
 * Parses the sub-packets of an operator packet by either length or count.
 */
private val subPacketsParser: Parser<List<Packet>> =
  subPacketsByLengthParser or subPacketsByCountParser

/**
 * Parses an operator packet by combining the version, type ID, and sub-packets.
 * It filters the parsed packet to ensure that the type ID is not 4 (indicating a literal packet).
 */
private val operatorPacketParser: Parser<Packet.Operator> =
  zip(versionParser, typeIdParser, subPacketsParser)
    .filter { (_, typeId, _) -> typeId != 4L }
    .map { (version, typeId, subPackets) -> Packet.Operator(version, typeId, subPackets) }

/**
 * Parses a packet, which can be either a literal or an operator packet.
 */
private val packetParser: Parser<Packet> = literalPacketParser or operatorPacketParser

// endregion

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
