package com.github.hsz.aoc

import com.github.hsz.aoc.utils.Resources

abstract class Day(val number: Number) {

    val input = Resources.asString("day${number.toString().padStart(2, '0')}.txt")

    abstract fun part1(input: String): Any

    abstract fun part2(input: String): Any
}