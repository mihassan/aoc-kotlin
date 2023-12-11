@file:Suppress("PackageDirectoryMismatch")

package aoc23.day07

import lib.Collections.histogram
import lib.Solution

enum class Card(val symbol: Char) {
  JOKER('X'),
  TWO('2'),
  THREE('3'),
  FOUR('4'),
  FIVE('5'),
  SIX('6'),
  SEVEN('7'),
  EIGHT('8'),
  NINE('9'),
  TEN('T'),
  JACK('J'),
  QUEEN('Q'),
  KING('K'),
  ACE('A');

  companion object {
    fun parse(symbol: Char): Card {
      return values().find { it.symbol == symbol } ?: error("Invalid symbol: $symbol")
    }
  }
}

enum class HandType {
  HIGH_CARD,
  ONE_PAIR,
  TWO_PAIRS,
  THREE_OF_A_KIND,
  FULL_HOUSE,
  FOUR_OF_A_KIND,
  FIVE_OF_A_KIND
}

data class Hand(val cards: List<Card>, val bid: Long) {
  fun type(): HandType {
    val counts = cards.histogram()

    return when (counts.size) {
      1 -> HandType.FIVE_OF_A_KIND
      2 -> if (counts.values.contains(4)) HandType.FOUR_OF_A_KIND else HandType.FULL_HOUSE
      3 -> if (counts.values.contains(3)) HandType.THREE_OF_A_KIND else HandType.TWO_PAIRS
      4 -> HandType.ONE_PAIR
      5 -> HandType.HIGH_CARD
      else -> error("Invalid hand: $this")
    }
  }

  fun jackType(): HandType {
    val counts = cards.histogram()
    val jackCount = counts[Card.JACK] ?: 0
    val nonJackCounts = counts.filterKeys { it != Card.JACK }

    return when (jackCount) {
      0 -> type()
      5 -> HandType.FIVE_OF_A_KIND
      else -> {
        val mostCommonCard = nonJackCounts.maxByOrNull { it.value }!!.key
        val updatedCards = cards.map { if (it == Card.JACK) mostCommonCard else it }
        Hand(updatedCards, bid).type()
      }
    }
  }

  fun plainComparator(other: Hand): Int {
    if (type() != other.type()) {
      return type().compareTo(other.type())
    }

    cards.zip(other.cards).forEach { (a, b) ->
      if (a != b) {
        return a.compareTo(b)
      }
    }

    return 0
  }

  fun specialJackComparator(other: Hand): Int {
    if (jackType() != other.jackType()) {
      return jackType().compareTo(other.jackType())
    }

    val thisCards = cards.map { if (it == Card.JACK) Card.JOKER else it }
    val otherCards = other.cards.map { if (it == Card.JACK) Card.JOKER else it }

    thisCards.zip(otherCards).forEach { (a, b) ->
      if (a != b) {
        return a.compareTo(b)
      }
    }

    return 0
  }

  companion object {
    fun parse(handStr: String): Hand {
      HandType.entries.toList().sorted()
      val (cards, bid) = handStr.split(" ")
      return Hand(cards.map { Card.parse(it) }, bid.toLong())
    }
  }
}

typealias Input = List<Hand>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2023, "Day07") {
  override fun parse(input: String): Input = input.lines().map { Hand.parse(it) }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.sortedWith(Hand::plainComparator)
      .mapIndexed { index, hand -> (index + 1) * hand.bid }.sum()

  override fun part2(input: Input): Output =
    input.sortedWith(Hand::specialJackComparator)
      .mapIndexed { index, hand -> (index + 1) * hand.bid }.sum()
}

fun main() = solution.run()
