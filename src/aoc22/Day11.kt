@file:Suppress("PackageDirectoryMismatch")

package aoc22.day11

import lib.Solution

private class Game private constructor(
  val modulus: Long,
  val monkeys: MutableList<Monkey> = mutableListOf(),
) {
  private lateinit var onInspectionHandler: ((MonkeyId, Item) -> Item)

  fun runMultipleRounds(rounds: Int) = repeat(rounds) { runSingleRound() }

  fun onInspection(onInspectionHandler: (MonkeyId, Item) -> Item) {
    this.onInspectionHandler = onInspectionHandler
  }

  fun clone(): Game {
    val game = Game(modulus)
    monkeys.forEach { monkey ->
      game.monkeys += monkey.clone(throwItemToFn = game::throwItemTo)
    }
    return game
  }

  private fun runSingleRound() =
    monkeys.forEach { monkey -> monkey.processItems(onInspectionHandler) }

  private fun throwItemTo(item: Item, monkeyId: MonkeyId) = monkeys[monkeyId].receive(item)

  private fun addMonkey(monkey: Monkey) = monkeys.add(monkey)

  companion object {
    fun parse(gameConfig: String): Game {
      val modulus = Regex("divisible by (\\d+)")
        .findAll(gameConfig)
        .map { it.groupValues[1].toLong() }
        .reduce(Long::times)
      val game = Game(modulus)
      gameConfig.split("\n\n").forEach {
        val monkey = Monkey.parse(it, game::throwItemTo)
        game.addMonkey(monkey)
      }
      return game
    }
  }
}

private data class Monkey(
  val id: MonkeyId,
  val items: MutableList<Item>,
  val operation: Operation,
  val test: Test,
  val throwItemToFn: (Item, MonkeyId) -> Unit,
) {
  fun processItems(onInspectionHandler: (MonkeyId, Item) -> Item) {
    items.forEach { item -> processItem(item, onInspectionHandler) }
    items.clear()
  }

  fun clone(throwItemToFn: (Item, MonkeyId) -> Unit): Monkey {
    return Monkey(id, items.toMutableList(), operation, test, throwItemToFn)
  }

  private fun processItem(item: Item, onInspectionHandler: ((MonkeyId, Item) -> Item)) {
    val inspectedItem = inspect(item)
    val itemToTest = onInspectionHandler(id, inspectedItem)
    val monkeyId = test.test(itemToTest)
    throwItemToFn(itemToTest, monkeyId)
  }

  private fun inspect(item: Item) = Item(operation.operate(item.worryLevel))

  fun receive(item: Item) {
    items += item
  }

  companion object {
    fun parse(monkeyConfig: String, throwItemToFn: (Item, MonkeyId) -> Unit): Monkey {
      val lines = monkeyConfig.lines()
      val id = Regex("\\d+").find(lines[0])!!.value.toInt()
      val items = Item.parseMultipleItems(lines[1]).toMutableList()
      val operation = Operation.parse(lines[2])
      val test = Test.parse(lines.takeLast(3).joinToString())

      return Monkey(id, items, operation, test, throwItemToFn)
    }
  }
}

private typealias MonkeyId = Int

private data class Item(val worryLevel: Long) {
  companion object {
    fun parseSingleItem(str: String): Item = Item(str.toLong())

    fun parseMultipleItems(str: String): List<Item> =
      Regex("\\d+").findAll(str).map { parseSingleItem(it.value) }.toList()
  }
}

private sealed interface Operation {
  fun operate(value: Long): Long

  private object Square : Operation {
    override fun operate(value: Long): Long = value * value
  }

  private data class Multiply(val multiplier: Long) : Operation {
    override fun operate(value: Long): Long = value * multiplier
  }

  private data class Add(val summand: Long) : Operation {
    override fun operate(value: Long): Long = value + summand
  }

  companion object {
    const val SQUARE_PREFIX = "old * old"
    const val MULTIPLY_PREFIX = "old * "
    const val ADD_PREFIX = "old + "

    fun parse(str: String): Operation {
      return when {
        SQUARE_PREFIX in str -> Square
        MULTIPLY_PREFIX in str -> Multiply(str.substringAfter(MULTIPLY_PREFIX).toLong())
        ADD_PREFIX in str -> Add(str.substringAfter(ADD_PREFIX).toLong())
        else -> error("Invalid input")
      }
    }
  }
}

private data class Test(val modulus: Long, val trueCase: MonkeyId, val falseCase: MonkeyId) {
  fun test(item: Item): MonkeyId =
    if (item.worryLevel % modulus == 0L) trueCase else falseCase

  companion object {
    fun parse(str: String): Test {
      val (modulus, trueCase, falseCase) =
        Regex("\\d+").findAll(str).map { it.value.toInt() }.toList()
      return Test(modulus.toLong(), trueCase, falseCase)
    }
  }
}

private typealias Input = Game

private typealias Output = Long

private val solution = object : Solution<Input, Output>(2022, "Day11") {
  override fun parse(input: String): Input = Game.parse(input)

  override fun format(output: Output): String {
    return "$output"
  }

  override fun part1(input: Input): Output {
    val game = input.clone()
    val inspectionCount = mutableMapOf<MonkeyId, Long>()
    game.onInspection { monkeyId, item ->
      inspectionCount[monkeyId] = inspectionCount[monkeyId]?.let { it + 1 } ?: 1
      Item(item.worryLevel / 3L)
    }
    game.runMultipleRounds(20)
    val activeMonkeys = inspectionCount.values.sortedDescending().take(2)
    return activeMonkeys.reduce(Long::times)
  }

  override fun part2(input: Input): Output {
    val game = input.clone()
    val inspectionCount = mutableMapOf<MonkeyId, Long>()
    game.onInspection { monkeyId, item ->
      inspectionCount[monkeyId] = inspectionCount[monkeyId]?.let { it + 1 } ?: 1
      Item(item.worryLevel % game.modulus)
    }
    game.runMultipleRounds(10000)
    val activeMonkeys = inspectionCount.values.sortedDescending().take(2)
    return activeMonkeys.reduce(Long::times)
  }
}

fun main() = solution.run()
