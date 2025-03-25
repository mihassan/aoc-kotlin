import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import kotlin.io.path.*
import okhttp3.*

class FetchInput : CliktCommand(name = "./gradlew fetchInput") {
  val year: Int by option().int().restrictTo(21..24).required()
  val day: Int by option().int().restrictTo(1..25).required()

  override fun run() {
    println("Fetching input for $year day $day ...")

    if (System.getenv("AOC_SESSION") == null) {
      error("AOC_SESSION environment variable not set")
    }

    val client = OkHttpClient()
    val request: Request = Request.Builder()
      .url("https://adventofcode.com/20$year/day/$day/input")
      .header("Cookie", "session=${System.getenv("AOC_SESSION")}")
      .header("User-Agent", "https://github.com/mihassan/aoc-kotlin by mihassan@gmail.com")
      .build()

    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) error("Unexpected code $response")

      val path = Path(String.format("src/data/aoc%02d/Day%02d.txt", year, day))
      if (!path.parent.exists()) {
        println("Creating directory ${path.parent}")
        path.parent.createDirectories()
      }

      path.writeText(response.body!!.string().trim())
      println("Input saved to $path")
    }
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) = FetchInput().main(args)
  }
}
