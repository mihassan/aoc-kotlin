package tool

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

class PrepareYear : CliktCommand(name = "./gradlew prepareYear") {
  val year: Int by option().int().restrictTo(15..99).default(25)
  val overwrite: Boolean by option().flag(default = false)

  override fun run() {
    println("Preparing AoC 20$year...")

    createDirectories()
    createSolutionTemplates()

    println("\nDone! Don't forget to update SOLVED_YEARS in Solve.kt and FetchInput.kt if needed.")
  }

  private fun createDirectories() {
    val srcDir = Path("src/aoc$year")
    val dataDir = Path("src/data/aoc$year")

    listOf(srcDir, dataDir).forEach { dir ->
      if (!dir.exists()) {
        println("Creating directory: $dir")
        dir.createDirectories()
      } else {
        println("Directory already exists: $dir")
      }
    }
  }

  private fun createSolutionTemplates() {
    (1..25).forEach { day ->
      createSolutionTemplate(day)
    }
  }

  private fun createSolutionTemplate(day: Int) {
    val fileName = "Day%02d.kt".format(day)
    val filePath = Path("src/aoc$year/$fileName")

    if (filePath.exists() && !overwrite) {
      println("Solution file already exists (use --overwrite to replace): $filePath")
      return
    }

    val template = generateTemplate(day)
    filePath.writeText(template)
    println("Created solution template: $filePath")
  }

  private fun generateTemplate(day: Int): String {
    val dayFormatted = "%02d".format(day)
    return """
      |@file:Suppress("PackageDirectoryMismatch")
      |
      |package aoc$year.day$dayFormatted
      |
      |import lib.Solution
      |
      |typealias Input = List<String>
      |
      |typealias Output = Int
      |
      |private val solution = object : Solution<Input, Output>(20$year, "Day$dayFormatted") {
      |  override fun parse(input: String): Input = input.lines()
      |
      |  override fun format(output: Output): String = "${"$"}output"
      |
      |  override fun part1(input: Input): Output {
      |    TODO("Implement part 1")
      |  }
      |
      |  override fun part2(input: Input): Output {
      |    TODO("Implement part 2")
      |  }
      |}
      |
      |fun main() = solution.run()
      |""".trimMargin()
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) = PrepareYear().main(args)
  }
}
