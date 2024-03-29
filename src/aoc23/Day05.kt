@file:Suppress("PackageDirectoryMismatch")

package aoc23.day05

import kotlin.math.max
import kotlin.math.min
import lib.Collections.headTail
import lib.Solution
import lib.Strings.extractLongs
import lib.Strings.longs

enum class Category {
  SEED, SOIL, FERTILIZER, WATER, LIGHT, TEMPERATURE, HUMIDITY, LOCATION;

  companion object {
    fun parse(categoryStr: String) = Category.valueOf(categoryStr.uppercase())
  }
}

data class Item(val category: Category, val number: Long)

data class RangeMap(val srcRange: LongRange, val destRange: LongRange) {
  constructor(destRangeStart: Long, srcRangeStart: Long, rangeLength: Long) :
    this(
      srcRangeStart..<srcRangeStart + rangeLength,
      destRangeStart..<destRangeStart + rangeLength
    )

  fun apply(srcNumber: Long): Long =
    when (srcNumber) {
      in srcRange -> srcNumber + destRange.first - srcRange.first
      else -> srcNumber
    }

  companion object {
    fun parse(rangeMapStr: String): RangeMap {
      val (destRangeStart, srcRangeStart, rangeLength) = rangeMapStr.longs()
      return RangeMap(destRangeStart, srcRangeStart, rangeLength)
    }
  }
}

data class CategoryMap(
  val srcCategory: Category,
  val destCategory: Category,
  val rangeMaps: List<RangeMap>,
) {
  fun apply(srcItem: Item): Item =
    when (srcItem.category) {
      srcCategory -> Item(destCategory, applyRangeMaps(srcItem.number))
      else -> srcItem
    }

  private fun applyRangeMaps(srcNumber: Long): Long =
    rangeMaps
      .firstOrNull { srcNumber in it.srcRange }
      ?.apply(srcNumber)
      ?: srcNumber

  companion object {
    fun parse(categoryMapStr: String): CategoryMap {
      val (headerStr, rangesStr) = categoryMapStr.lines().headTail()
      val (srcCategoryStr, destCategoryStr) = HEADER_REGEX.matchEntire(headerStr!!)!!.destructured

      val srcCategory = Category.parse(srcCategoryStr)
      val destCategory = Category.parse(destCategoryStr)
      val rangeMaps = rangesStr.map(RangeMap.Companion::parse)

      return CategoryMap(srcCategory, destCategory, rangeMaps)
    }

    private val HEADER_REGEX = """(\w+)-to-(\w+) map:""".toRegex()
  }
}

data class Almanac(val seeds: List<Long>, val categoryMaps: List<CategoryMap>) {
  fun convert(item: Item, destCategory: Category): Item {
    var convertedItem = item
    while (convertedItem.category != destCategory) {
      convertedItem = categoryMaps
        .first { it.srcCategory == convertedItem.category }
        .apply(convertedItem)
    }
    return convertedItem
  }

  companion object {
    fun parse(almanacStr: String): Almanac {
      val (seedsStr, categoriesStr) = almanacStr.split("\n\n").headTail()
      val seeds = seedsStr!!.extractLongs()
      val categoryMaps = categoriesStr.map(CategoryMap.Companion::parse)
      return Almanac(seeds, categoryMaps)
    }
  }
}

typealias Input = Almanac

typealias Output = Long

private val solution = object : Solution<Input, Output>(2023, "Day05") {
  override fun parse(input: String): Input = Almanac.parse(input)

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    input.seeds.map { seed ->
      input.convert(Item(Category.SEED, seed), Category.LOCATION)
    }.minOf {
      it.number
    }

  override fun part2(input: Input): Output {
    var result = Long.MAX_VALUE

    // Check all seeds in the given range which potentially skipping over some seeds.
    fun check(range: LongProgression): Long? {
      var minSeed: Long? = null
      var minLocation = result

      for (seed in range) {
        val location = input.convert(Item(Category.SEED, seed), Category.LOCATION)
        if (location.number < minLocation) {
          minSeed = seed
          minLocation = location.number
        }
      }

      result = min(result, minLocation)

      return minSeed
    }

    input.seeds.chunked(2)
      .forEach { (start, length) ->
        if (length < 1000000) {
          // For small ranges, check all seeds.
          check(start..<start + length)
        } else {
          // For large ranges, check in two stages.
          // Stage 1: Jump by 1,000 to get closer to the minimum seed.
          check(start..<start + length step 1000)?.let { minSeed ->
            // Stage 2: Check last 1,000 seeds (inclusive of minSeed) which we skipped earlier.
            // Care should be taken to not go past start.
            check(max(minSeed - 999, start)..minSeed)
          }
        }
      }

    return result
  }
}

fun main() = solution.run()
