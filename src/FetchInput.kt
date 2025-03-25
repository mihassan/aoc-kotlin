import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import java.nio.file.Path
import kotlin.io.path.*
import okhttp3.*

class AocClient {
  val session: String

  init {
    if (System.getenv("AOC_SESSION") == null) {
      error("AOC_SESSION environment variable not set")
    }
    session = System.getenv("AOC_SESSION")
  }

  fun getInput(year: Int, day: Int): String {
    val client = OkHttpClient()
    val request = makeRequest("https://adventofcode.com/20$year/day/$day/input")

    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) error("Unexpected code $response")
      response.body?.let {
        return it.string().trim()
      } ?: error("Empty response")
    }
  }

  private fun makeRequest(url: String): Request =
    Request.Builder()
      .url(url)
      .header("Cookie", "session=$session")
      .header("User-Agent", "$REPOSITORY by $EMAIL")
      .build()

  companion object {
    private const val  REPOSITORY = "https://github.com/mihassan/aoc-kotlin"
    private const val EMAIL = "mihassan@gmail.com"
  }
}

private class DataClient {
  fun save(year: Int, day: Int, input: String) = prepare(year, day).writeText(input.trim())

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
  val day: Int by option().int().restrictTo(1..25).required()

  override fun run() {
    println("Fetching input for $year day $day ...")
    val input = AocClient().getInput(year, day)

    println("Input fetched. Saving ...")
    DataClient().save(year, day, input)

    println("Input saved")
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) = FetchInput().main(args)
  }
}
