package tool

import io.github.cdimascio.dotenv.dotenv
import lib.Solution
import okhttp3.OkHttpClient
import okhttp3.Request

class AocClient {
  private val session: String = dotenv { ignoreIfMissing = true }.get("AOC_SESSION")
    ?: error("AOC_SESSION is neither found in .env nor set as an environment variable")

  fun getInput(year: Int, day: Int): String {
    val client = OkHttpClient()
    val request = makeRequest("https://adventofcode.com/20$year/day/$day/input")

    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) error("Unexpected code $response while fetching input")
      response.body?.let {
        return it.string().trimEnd()
      } ?: error("Empty response body while fetching input")
    }
  }

  fun getSolution(year: Int, day: Int, part: Solution.Part): String? {
    val problemStatement = getProblemStatement(year, day)
    val matches =  SOLUTION_REGEX.findAll(problemStatement)
    return when (part) {
      Solution.Part.PART1 -> matches.elementAtOrNull(0)?.groupValues?.get(1)
      Solution.Part.PART2 -> matches.elementAtOrNull(1)?.groupValues?.get(1)
    }
  }

  private fun getProblemStatement(year: Int, day: Int): String {
    val client = OkHttpClient()
    val request = makeRequest("https://adventofcode.com/20$year/day/$day")

    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) error("Unexpected code $response while fetching problem statement")
      response.body?.let {
        return it.string()
      } ?: error("Empty response body while fetching problem statement")
    }
  }

  private fun makeRequest(url: String): Request =
    Request.Builder()
      .url(url)
      .header("Cookie", "session=$session")
      .header("User-Agent", "$REPOSITORY by $EMAIL")
      .build()

  companion object {
    private const val REPOSITORY = "https://github.com/mihassan/aoc-kotlin"
    private const val EMAIL = "mihassan@gmail.com"
    private val SOLUTION_REGEX = Regex("""<p>Your puzzle answer was <code>(\w+)</code>.""")
  }
}