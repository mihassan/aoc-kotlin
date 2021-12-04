import java.io.File

private val SPACE_REGEX = "\\s+".toRegex()

fun readInput(name: String) = File("src", "$name.txt").readLines()

fun readWords(name: String) = readInput(name).flatMap(String::words)

fun readInts(name: String) = readWords(name).map(String::toInt)

fun String.words() = split(SPACE_REGEX)

fun <K> Map<K, Int>.getOrZero(key: K) = getOrDefault(key, 0)

fun <T> Iterable<T>.freq() = groupingBy { it }.eachCount()
