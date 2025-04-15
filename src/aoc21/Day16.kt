@file:Suppress("PackageDirectoryMismatch")

package aoc21.day16

import lib.Parser
import lib.ParserResult
import lib.Solution
import lib.ParserCombinators.many
import lib.ParserCombinators.count

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

private fun packetParser(): Parser<Packet> = Parser { state ->
  if (state.length < 6) return@Parser null

  val version = state.take(3).toLong(2)
  val typeId = state.drop(3).take(3).toLong(2)
  val nextState = state.drop(6)
  when (typeId) {
    4L -> literalPacketParser(version).parse(nextState)
    else -> operatorPacketParser(version, typeId).parse(nextState)
  }
}

private fun literalPacketParser(version: Long): Parser<Packet.Literal> =
  Parser { state ->
    val (value, nextState) = literalValueParser.parse(state) ?: return@Parser null
    ParserResult(Packet.Literal(version, value), nextState)
  }

private val literalValueParser: Parser<Long> = Parser { state ->
  val chunked = state.chunked(5)
  val chunksToConsume = chunked.indexOfFirst { it.first() == '0' } + 1
  if (chunksToConsume == 0) return@Parser null

  val value = chunked.take(chunksToConsume).joinToString("") { it.drop(1) }.toLong(2)
  val nextState = state.drop(chunksToConsume * 5)

  ParserResult(value, nextState)
}

private fun operatorPacketParser(version: Long, typeId: Long): Parser<Packet.Operator> =
  Parser { state ->
    if (state.isEmpty()) return@Parser null

    val (subPackets, nextState) = when (state.first()) {
      '0' -> {
        if (state.length < 16) return@Parser null

        val length = state.drop(1).take(15).toInt(2)
        val subPacketsState = state.drop(16).take(length)
        val (subPackets, _) = packetParser().many().parse(subPacketsState)
          ?: return@Parser null
        val nextState = state.drop(16 + length)

        subPackets to nextState
      }

      '1' -> {
        if (state.length < 12) return@Parser null

        val count = state.drop(1).take(11).toInt(2)
        val subPacketsState = state.drop(12)
        val (subPackets, nextState) = packetParser().count(count).parse(subPacketsState)
          ?: return@Parser null

        subPackets to nextState
      }

      else -> error("Invalid operator packet")
    }

    ParserResult(Packet.Operator.from(typeId, version, subPackets), nextState)
  }

private typealias Input = String
private typealias Output = Long

private val solution = object : Solution<Input, Output>(2021, "Day16") {
  override fun parse(input: String): Input {
    return input.trim().map { it.digitToInt(16).toString(2).padStart(4, '0') }.joinToString("")
  }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    val (packet, _) = packetParser().parse(input) ?: return 0
    return packet.totalVersion()
  }

  override fun part2(input: Input): Output {
    val (packet, _) = packetParser().parse(input) ?: return 0
    return packet.calculateValue()
  }
}

fun main() = solution.run()
