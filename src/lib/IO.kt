package lib

import java.io.File

object IO {
  // Input
  val reader = System.`in`.bufferedReader()

  fun readText() = reader.readText()

  fun readLn() = reader.readLine()

  fun readLines() = reader.readLines()
  fun readLines(n: Int) = List(n) { readLn() }

  // Output
  val writer = System.out.bufferedWriter()

  fun Boolean.yesNo() = if (this) "YES" else "NO"
  fun writeLn(s: String) = writer.write("$s\n")
}