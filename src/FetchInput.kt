import kotlin.io.path.*
import okhttp3.*

fun main(args: Array<String>) {
  if(args.size != 2) {
    println("Usage: <YY> <DD>")
    return
  }

  val year = args[0].toIntOrNull() ?: error("Invalid year (21-24)")
  if (year !in 21..24) error("Invalid year (21-24)")

  val day = args[1].toIntOrNull() ?: error("Invalid day (1-25)")
  if (day !in 1..25) error("Invalid day (1-25)")

  println("Fetching input for $year day $day ...")

  if(System.getenv("AOC_SESSION") == null) {
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
    if(!path.parent.exists()) {
      println("Creating directory ${path.parent}")
      path.parent.createDirectories()
    }

    path.writeText(response.body!!.string())
    println("Input saved to $path")
  }
}
