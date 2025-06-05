@file:Suppress("PackageDirectoryMismatch")

package aoc21.day20

import aoc21.day20.Pixel.Companion.toInt
import lib.Solution

private enum class Pixel(val char: Char, val value: Int) {
  LIGHT('#', 1),
  DARK('.', 0);

  companion object {
    fun parse(pixelChar: Char): Pixel = entries.first { it.char == pixelChar }

    fun List<Pixel>.toInt(): Int = fold(0) { acc, pixel -> (acc shl 1) or pixel.value }
  }
}

private data class Algorithm(val pixels: List<Pixel>) {
  operator fun get(index: Int): Pixel = pixels[index]

  companion object {
    fun parse(algorithmStr: String): Algorithm = Algorithm(algorithmStr.map(Pixel::parse))
  }
}

private data class Point(val row: Int, val col: Int)

private data class Image(val pixels: List<List<Pixel>>) {
  val height: Int = pixels.size
  val width: Int = pixels.firstOrNull()?.size ?: 0

  operator fun get(point: Point): Pixel =
    pixels.getOrNull(point.row)?.getOrNull(point.col) ?: error(
      "Point $point is out of bounds for image of size $height x $width"
    )

  /**
   * Pads the image with a border of the specified amount using DARK pixels.
   * This effectively increases the dimensions of the image by `amount * 2` in both width and height.
   * This is needed for the image enhancement process, as it allows the algorithm to
   * apply to the edges of the image without running out of bounds.
   */
  fun pad(amount: Int): Image {
    val newPixels = MutableList(height + 2 * amount) {
      MutableList(width + 2 * amount) { Pixel.DARK }
    }

    for (y in pixels.indices) {
      for (x in pixels[y].indices) {
        newPixels[y + amount][x + amount] = pixels[y][x]
      }
    }

    return Image(newPixels)
  }

  /**
   * Enhances the image using the provided algorithm.
   * The algorithm is applied to each pixel in the image, considering its 3x3 neighborhood.
   * The resulting pixel is determined by the algorithm based on the binary representation
   * of the neighborhood pixels.
   * This method assumes that the image has been padded appropriately before enhancement,
   * so that the algorithm can be applied to all pixels without running out of bounds.
   * The resulting image will be smaller than the original by 2 pixels in both dimensions,
   * as the outermost pixels are not included in the enhancement.
   */
  fun enhance(algorithm: Algorithm): Image {
    val newPixels = MutableList(height - 2) { MutableList(width - 2) { Pixel.DARK } }

    (1..<height - 1).forEach { y ->
      (1..<width - 1).forEach { x ->
        val index = (-1..1).flatMap { dy ->
          (-1..1).map { dx ->
            this[Point(y + dy, x + dx)]
          }
        }.toInt()
        newPixels[y - 1][x - 1] = algorithm[index]
      }
    }

    return Image(newPixels)
  }

  fun countLitPixels(): Int =
    pixels.sumOf { row -> row.count { it == Pixel.LIGHT } }

  companion object {
    fun parse(imageStr: String): Image =
      Image(imageStr.lines().map { line -> line.map(Pixel::parse) })
  }
}

private data class Input(
  val algorithm: Algorithm,
  val image: Image,
) {
  companion object {
    fun parse(input: String): Input =
      input.split("\n\n").let { (algorithmStr, imageStr) ->
        Input(Algorithm.parse(algorithmStr), Image.parse(imageStr))
      }
  }
}

private typealias Output = Int

private val solution = object : Solution<Input, Output>(2021, "Day20") {
  override fun parse(input: String): Input = Input.parse(input)

  override fun format(output: Output): String = "$output"

  override fun solve(part: Part, input: Input): Output {
    val steps = when (part) {
      Part.PART1 -> 2
      Part.PART2 -> 50
    }

    // We assume that after number of steps, the image will be surrounded by all DARK pixels.
    // However, at each step, a pixel can influence its neighbors up to 2 pixels away.
    // Therefore, we pad the image by 2 * steps.
    var image = input.image.pad(2 * steps)
    repeat(steps) {
      image = image.enhance(input.algorithm)
    }

    return image.countLitPixels()
  }
}

fun main() = solution.run()
