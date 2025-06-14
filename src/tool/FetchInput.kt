package tool

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

private class DataClient {
  fun exists(year: Int, day: Int): Boolean = Path(String.format(PATH_FORMAT, year, day)).exists()

  fun save(year: Int, day: Int, input: String) = prepare(year, day).writeText(input)

  private fun prepare(year: Int, day: Int): Path {
    val path = Path(String.format(PATH_FORMAT, year, day))

    if (!path.parent.exists()) {
      println("Creating directory ${path.parent}")
      path.parent.createDirectories()
    }

    return path
  }

  companion object {
    private const val PATH_FORMAT = "src/data/aoc%02d/Day%02d.txt"
  }
}

class FetchInput : CliktCommand(name = "./gradlew fetchInput") {
  val year: Int by option().int().restrictTo(21..24).required()
  val day: Int? by option().int().restrictTo(1..25)
  val overwrite: Boolean by option().flag(default = false)

  override fun run() {
    if (day == null) {
      fetchAllInputs(year)
    } else {
      fetchInput(year, day!!)
    }
  }

  private fun fetchInput(year: Int, day: Int) {
    if (!overwrite && DataClient().exists(year, day)) {
      println("Input for 20$year Day$day already exists. Use --overwrite to overwrite.")
      return
    }

    println("Fetching input for 20$year Day$day...")
    val input = AocClient().getInput(year, day)
    println("Input fetched.")

    println("Saving input...")
    DataClient().save(year, day, input)
    println("Input saved.")
  }

  private fun fetchAllInputs(year: Int) {
    val aocClient = AocClient()
    val dataClient = DataClient()

    println("Fetching all inputs for 20$year...")

    (1..25).forEach { day ->
      if (!overwrite && dataClient.exists(year, day)) {
        println("Input for 20$year Day$day already exists. Use --overwrite to overwrite.")
        return@forEach
      }

      val input = aocClient.getInput(year, day)
      println("Input for 20$year Day$day fetched.")
      println("Saving...")
      dataClient.save(year, day, input)
    }

    println("All inputs saved.")
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) = FetchInput().main(args)
  }
}
