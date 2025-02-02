@file:Suppress("PackageDirectoryMismatch")

package aoc24.day09

import lib.Solution

sealed interface Block {
  var size: Int
}

data class FreeBlock(override var size: Int) : Block
data class FileBlock(override var size: Int, val id: Int) : Block

typealias Input = List<Block>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2024, "Day09") {
  override fun parse(input: String): Input =
    input.mapIndexed { i, c ->
      if (i % 2 == 0)
        FileBlock(c.digitToInt(), i / 2)
      else
        FreeBlock(c.digitToInt())
    }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    var (i, j) = 0 to input.size - 1
    val fileBlocks = mutableListOf<FileBlock>()

    while (i <= j) {
      if (input[i] is FileBlock) {
        // Leftmost file block can not be moved.
        fileBlocks.add(input[i] as FileBlock)
        i += 1
      } else if (input[j] is FreeBlock) {
        // Rightmost free block can be skipped
        j -= 1
      } else {
        // Try moving rightmost file block to leftmost free block
        if (input[i].size > input[j].size) {
          // Free block has enough size to move the whole file block, and still have extra space.
          input[i].size -= input[j].size
          fileBlocks.add(input[j] as FileBlock)
          j -= 1
        } else if (input[i].size < input[j].size) {
          // Only part of the file block can be copied to the free block.
          input[j].size -= input[i].size
          fileBlocks.add(FileBlock(input[i].size, (input[j] as FileBlock).id))
          i += 1
        } else {
          // The file block can be fully moved to the free block without any free space left.
          fileBlocks.add(input[j] as FileBlock)
          i += 1
          j -= 1
        }
      }
    }

    var checkSum = 0L
    var k = 0
    fileBlocks.forEach { fileBlock ->
      repeat(fileBlock.size) {
        checkSum += k * fileBlock.id
        k += 1
      }
    }

    return checkSum
  }

  override fun part2(input: Input): Output = TODO()
}

fun main() = solution.run()
