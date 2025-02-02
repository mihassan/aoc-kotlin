@file:Suppress("PackageDirectoryMismatch")

package aoc24.day09

import java.util.LinkedList
import lib.Solution

sealed interface Block {
  val size: Long

  fun checksum(position: Long): Long

  companion object {
    fun newBlock(isFileBlock: Boolean, size: Long, id: Long): Block =
      when (isFileBlock) {
        true -> FileBlock(size, id)
        false -> FreeBlock(size)
      }
  }
}

data class FreeBlock(override val size: Long) : Block {
  override fun checksum(position: Long): Long = 0L

  fun place(fileBlock: FileBlock): FreeBlock? =
    copy(size = size - fileBlock.size).takeIf { it.size > 0 }
}

data class FileBlock(override val size: Long, val id: Long) : Block {
  override fun checksum(position: Long): Long = (position..<position + size).sum() * id

  fun moveTo(freeBlock: FreeBlock): Pair<FileBlock, FileBlock?> =
    if (size <= freeBlock.size)
      this to null
    else
      partiallyMove(freeBlock)

  private fun partiallyMove(freeBlock: FreeBlock): Pair<FileBlock, FileBlock> =
    copy(size = freeBlock.size) to copy(size = size - freeBlock.size)
}

typealias Input = List<Block>

typealias Output = Long

private val solution = object : Solution<Input, Output>(2024, "Day09") {
  override fun parse(input: String): Input {
    var nextFileId = 0L
    var nextIsFileBlock = true

    return buildList {
      input.forEach {
        val size = it.digitToInt().toLong()

        add(Block.newBlock(nextIsFileBlock, size, nextFileId))

        if (nextIsFileBlock) {
          nextFileId += 1
        }
        nextIsFileBlock = nextIsFileBlock.not()
      }
    }
  }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output =
    compact(input).checksum()

  override fun part2(input: Input): Output = TODO()

  private fun compact(inputBlocks: List<Block>): Sequence<Block> = sequence {
    // A doubly linked list is convenient as we can add/remove from both ends. This effectively
    // similar to a 2-pointer solution.
    val blocks = LinkedList(inputBlocks)

    while (!blocks.isEmpty()) {
      when (blocks.first) {
        is FileBlock ->
          // Leftmost file blocks is fixed and won't be moved anymore. So, it is safe to yield them.
          yield(blocks.removeFirst())

        is FreeBlock ->
          when (blocks.last) {
            is FreeBlock ->
              // Rightmost free blocks can be ignored.
              blocks.removeLast()

            is FileBlock ->
              // This is the most important case where we move the rightmost file block to the
              // leftmost free block.
              yield(
                blocks.move(
                  blocks.removeFirst() as FreeBlock,
                  blocks.removeLast() as FileBlock
                )
              )
          }
      }
    }
  }

  private fun LinkedList<Block>.move(freeBlock: FreeBlock, fileBlock: FileBlock): FileBlock {
    val remainingFreeBlock = freeBlock.place(fileBlock)
    if (remainingFreeBlock != null) addFirst(remainingFreeBlock)

    val (movedFileBlock, remainingFileBlock) = fileBlock.moveTo(freeBlock)
    if (remainingFileBlock != null) addLast(remainingFileBlock)

    return movedFileBlock
  }

  private fun Sequence<Block>.checksum(): Long {
    var currPos = 0L
    return sumOf { block ->
      block.checksum(currPos).also {
        currPos += block.size
      }
    }
  }
}


fun main() = solution.run()
