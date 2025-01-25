@file:Suppress("PackageDirectoryMismatch")

package aoc22.day11

import lib.Solution

private class Game private constructor(
  val modulus: Long,
  val monkeys: List<Monkey>,
) {
  fun runMultipleRounds(rounds: Int, onInspectionHandler: (MonkeyId, Item) -> Item) =
    repeat(rounds) { runSingleRound(onInspectionHandler) }

  private fun runSingleRound(onInspectionHandler: (MonkeyId, Item) -> Item) =
    monkeys.forEach { monkey -> monkey.processItems(onInspectionHandler, ::throwItemTo) }

  private fun throwItemTo(item: Item, monkeyId: MonkeyId) = monkeys[monkeyId].receive(item)

  companion object {
    fun parse(gameConfig: String): Game {
      val monkeys = gameConfig.split("\n\n").map { Monkey.parse(it) }
      val modulus = monkeys.map { it.test.modulus }.distinct().reduce(Long::times)

      return Game(modulus, monkeys)
    }
  }
}

private data class Monkey(
  val id: MonkeyId,
  val items: MutableList<Item>,
  val operation: Operation,
  val test: Test,
) {
  fun processItems(
    onInspectionHandler: (MonkeyId, Item) -> Item,
    throwItemToFn: (Item, MonkeyId) -> Unit,
  ) {
    items.forEach { item -> processItem(item, onInspectionHandler, throwItemToFn) }
    items.clear()
  }

  private fun processItem(
    item: Item,
    onInspectionHandler: (MonkeyId, Item) -> Item,
    throwItemToFn: (Item, MonkeyId) -> Unit,
  ) {
    val inspectedItem = inspect(item)
    val itemToTest = onInspectionHandler(id, inspectedItem)
    val monkeyId = test.test(itemToTest)
    throwItemToFn(itemToTest, monkeyId)
  }

  private fun inspect(item: Item) = item.operate(operation)

  fun receive(item: Item) {
    items += item
  }

  companion object {
    fun parse(monkeyConfig: String): Monkey {
      val lines = monkeyConfig.lines()
      val id = Regex("\\d+").find(lines[0])!!.value.toInt()
      val items = Item.parseMultipleItems(lines[1]).toMutableList()
      val operation = Operation.parse(lines[2])
      val test = Test.parse(lines.takeLast(3).joinToString())

      return Monkey(id, items, operation, test)
    }
  }
}

private typealias MonkeyId = Int

private data class Item(val worryLevel: Long) {
  fun operate(operation: Operation) = Item(operation.operate(worryLevel))

  companion object {
    fun parseSingleItem(str: String): Item = Item(str.toLong())

    fun parseMultipleItems(str: String): List<Item> =
      Regex("\\d+").findAll(str).map { parseSingleItem(it.value) }.toList()
  }
}

private sealed interface Operation {
  fun operate(value: Long): Long

  private data object Square : Operation {
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
  val ROUNDS = mapOf(Part.PART1 to 20, Part.PART2 to 10000)

  override fun parse(input: String): Input = Game.parse(input)

  override fun format(output: Output): String {
    return "$output"
  }

  override fun solve(part: Part, input: Input): Output {
    val inspectionCount = input.monkeys.map { it.id }.associateWith { 0L }.toMutableMap()

    input.runMultipleRounds(ROUNDS[part]!!) { monkeyId, item ->
      inspectionCount[monkeyId] = inspectionCount[monkeyId]!! + 1

      when (part) {
        Part.PART1 -> Item(item.worryLevel / 3L)
        Part.PART2 -> Item(item.worryLevel % input.modulus)
      }
    }

    val activeMonkeys = inspectionCount.values.sortedDescending().take(2)
    return activeMonkeys.reduce(Long::times)
  }
}

fun main() = solution.run()
