@file:Suppress("PackageDirectoryMismatch")

package aoc22.day07

import lib.Collections.partitions
import lib.Solution
import lib.Strings.words

sealed interface Command {
  object LsCommand : Command

  data class CdCommand(val path: String) : Command

  companion object {
    const val PREFIX = "$ "

    fun parse(line: String): Command {
      val parts = line.words()

      assert(parts.size in 2..3)
      assert(parts[0] == PREFIX)

      return when (parts[1]) {
        "ls" -> {
          assert(parts.size == 2)
          LsCommand
        }
        "cd" -> {
          assert(parts.size == 3)
          CdCommand(parts[2])
        }
        else -> throw java.lang.AssertionError()
      }
    }
  }
}

sealed class Entry {
  abstract val path: String

  class FileEntry(override val path: String, val size: Int) : Entry()
  class DirEntry(override val path: String) : Entry()

  companion object {
    fun parse(line: String): Entry {
      val parts = line.words()

      assert(parts.size == 2)

      return if (parts[0] == "dir") {
        DirEntry(parts[1])
      } else {
        FileEntry(parts[1], parts[0].toInt())
      }
    }
  }
}

data class CommandResult(val entries: List<Entry>) {
  companion object {
    fun parse(lines: List<String>): CommandResult {
      val entries = lines.map { line -> Entry.parse(line) }
      return CommandResult(entries)
    }
  }
}

data class ExecutedCommand(val command: Command, val result: CommandResult) {
  companion object {
    fun parse(lines: List<String>): ExecutedCommand {
      assert(lines.isNotEmpty())

      val command = Command.parse(lines.first())
      val result = CommandResult.parse(lines.drop(1))

      return ExecutedCommand(command, result)
    }
  }
}

typealias Input = List<ExecutedCommand>

typealias Output = Int

sealed class FileTree : Iterable<FileTree> {
  abstract val fullPath: String
  abstract val parent: FileTree?
  abstract val totalSize: Int

  data class FileNode(
    override val fullPath: String,
    override val parent: FileTree,
    val size: Int,
  ) : FileTree() {
    override val totalSize: Int = size

    override fun iterator() = iterator<FileTree> {
      yield(this@FileNode)
    }
  }

  data class DirNode(
    override val fullPath: String,
    override val parent: FileTree?,
    val children: MutableList<FileTree> = mutableListOf(),
  ) : FileTree() {
    override fun toString(): String = "DirNode: $fullPath with ${children.size} children"

    override val totalSize: Int by lazy { children.sumOf { it.totalSize } }

    override fun iterator() = iterator {
      yield(this@DirNode)
      children.forEach { yieldAll(it) }
    }

    fun addChild(child: FileTree) {
      children.add(child)
    }
  }

  companion object {
    const val ROOT_PATH = "/"
    const val PARENT_PATH = ".."
    const val PATH_SEPERATOR = "/"

    val RootNode: DirNode = DirNode(ROOT_PATH, null)
  }
}

fun walkFileTree(
  executedCommands: List<ExecutedCommand>,
  block: (parentPath: String, fullPath: String, entry: Entry) -> Unit,
) {
  val cwd = mutableListOf<String>()

  fun handleCdCommand(cdPath: String) {
    when (cdPath) {
      FileTree.ROOT_PATH -> cwd.clear()
      FileTree.PARENT_PATH -> cwd.removeLast()
      else -> cwd.add(cdPath)
    }
  }

  fun handleLsCommand(result: CommandResult): Unit {
    result.entries.forEach { entry ->
      val parentPath = cwd.joinToString(FileTree.PATH_SEPERATOR, prefix = FileTree.ROOT_PATH)
      val fullPath =
        (cwd + entry.path).joinToString(FileTree.PATH_SEPERATOR, prefix = FileTree.ROOT_PATH)
      block(parentPath, fullPath, entry)
    }
  }

  executedCommands.forEach { (command, result) ->
    when (command) {
      is Command.CdCommand -> handleCdCommand(command.path)
      Command.LsCommand -> handleLsCommand(result)
    }
  }
}


private fun constructEdgelessFileTree(executedCommands: List<ExecutedCommand>): Map<String, FileTree> =
  buildMap {
    set(FileTree.ROOT_PATH, FileTree.RootNode)
    walkFileTree(executedCommands) { parentPath, fullPath, entry ->
      val parent = checkNotNull(get(parentPath))
      when (entry) {
        is Entry.DirEntry -> set(fullPath, FileTree.DirNode(fullPath, parent))
        is Entry.FileEntry -> set(fullPath, FileTree.FileNode(fullPath, parent, entry.size))
      }
    }
  }


fun constructFileTree(executedCommands: List<ExecutedCommand>): FileTree {
  val graph = constructEdgelessFileTree(executedCommands)

  walkFileTree(executedCommands) { parentPath, fullPath, _ ->
    val parent = checkNotNull(graph[parentPath] as? FileTree.DirNode)
    val child = checkNotNull(graph[fullPath])
    parent.addChild(child)
  }

  return checkNotNull(graph[FileTree.ROOT_PATH])
}

private val solution = object : Solution<Input, Output>(2022, "Day07") {
  override fun parse(input: String): Input =
    input
      .lines()
      .partitions { it.startsWith(Command.PREFIX) }
      .map { ExecutedCommand.parse(it) }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    return constructFileTree(input)
      .filterIsInstance<FileTree.DirNode>()
      .map { it.totalSize }
      .filter { it <= 100000 }
      .sum()
  }

  override fun part2(input: Input): Output {
    val fileTree = constructFileTree(input)
    val required = fileTree.totalSize - 40000000
    return fileTree
      .filterIsInstance<FileTree.DirNode>()
      .map { it.totalSize }
      .filter { it >= required }
      .min()
  }
}

fun main() = solution.run()
