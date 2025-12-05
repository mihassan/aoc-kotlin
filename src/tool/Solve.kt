package tool

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import java.lang.reflect.Method
import lib.Solution

class Solve : CliktCommand(name = "./gradlew solve") {
  val year: Int? by option().int().restrictTo(SOLVED_YEARS)
  val day: Int? by option().int().restrictTo(DAYS_IN_YEAR)
  val fetchInput: Boolean by option().flag(default = false)
  val overwrite: Boolean by option().flag(default = false).validate {
      require(it implies fetchInput) {
        "--overwrite can be only used with --fetch-input"
      }
    }

  private infix fun Boolean.implies(other: Boolean): Boolean = if (this) other else true

  override fun run() {
    when {
      year == null -> runAllSolutions()
      day == null -> runAllSolutionsForYear(year!!)
      else -> runSolution(year!!, day!!)
    }
  }

  private fun runAllSolutions() {
    SOLVED_YEARS.forEach { year ->
      runAllSolutionsForYear(year)
    }
  }

  private fun runAllSolutionsForYear(year: Int) {
    DAYS_IN_YEAR.forEach { day ->
      runSolution(year, day)
    }
  }

  private fun runSolution(year: Int, day: Int) {
    if (fetchInput) {
      if (overwrite) FetchInput.main(arrayOf("--year=$year", "--day=$day", "--overwrite"))
      else FetchInput.main(arrayOf("--year=$year", "--day=$day"))
    }

    val mainFunction = findMainFunction(year, day)
    if (mainFunction == null) {
      println("20%2d Day%02d has not been solved yet".format(year, day))
      return
    }
    mainFunction.invoke(null)

    val aocClient = AocClient()
    val solution1 = aocClient.getSolution(year, day, Solution.Part.PART1) ?: "UNSOLVED"
    val solution2 = aocClient.getSolution(year, day, Solution.Part.PART2) ?: "UNSOLVED"
    println("    Solutions: [PART1] $solution1 | [PART2] $solution2")
    println(List(60) { "=" }.joinToString(""))
  }

  fun findMainFunction(year: Int, day: Int): Method? {
    val packageName = "aoc%2d.day%02d".format(year, day)
    val className = "%s.Day%02dKt".format(packageName, day)
    val mainClass = try {
      Class.forName(className)
    } catch (_: ClassNotFoundException) {
      return null
    }

    return mainClass.declaredMethods.find { it.name == "main" && it.parameterTypes.isEmpty() }
  }

  companion object {
    val SOLVED_YEARS = 21..25
    val DAYS_IN_YEAR = 1..25

    @JvmStatic
    fun main(args: Array<String>) = Solve().main(args)
  }
}
