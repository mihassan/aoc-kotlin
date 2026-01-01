@file:Suppress("PackageDirectoryMismatch")

package aoc23.day08

import lib.Maths.lcm
import lib.ProblemInput
import lib.Solution

private enum class Step(val symbol: Char) {
  LEFT('L'), RIGHT('R');

  companion object {
    fun parse(symbol: Char): Step = entries.find { it.symbol == symbol }!!
  }
}

private data class Node(val label: String, val left: String, val right: String) {
  fun isSource(): Boolean = label == "AAA"

  fun isGhostSource(): Boolean = label.endsWith("A")

  fun isSink(): Boolean = label == "ZZZ"

  fun isGhostSink(): Boolean = label.endsWith("Z")

  companion object {
    fun parse(nodeStr: String): Node {
      val (label, left, right) = NODE_REGEX.matchEntire(nodeStr)!!.destructured
      return Node(label, left, right)
    }

    private val NODE_REGEX = Regex("""(\w+) = \((\w+), (\w+)\)""")
  }
}

private data class Network(val nodes: Map<String, Node>) {
  fun source(): Node = nodes.values.find { it.isSource() }!!

  fun ghostSources(): List<Node> = nodes.values.filter { it.isGhostSource() }

  fun step(node: Node, step: Step): Node = when (step) {
    Step.LEFT -> nodes[node.left]
    Step.RIGHT -> nodes[node.right]
  } ?: error("Invalid step $step for node $node")

  companion object {
    fun parse(networkStr: ProblemInput): Network =
      Network(networkStr.linesAs(Node::parse).associateBy { it.label })
  }
}

private data class Input(val steps: List<Step>, val network: Network) {
  fun countStepsToSink(source: Node): Long {
    var node = source
    var stepIndex = 0L

    while (!node.isGhostSink()) {
      val step = getStep(stepIndex)
      node = network.step(node, step)
      stepIndex++
    }

    return stepIndex
  }

  private fun getStep(stepIndex: Long): Step = steps[(stepIndex % steps.size).toInt()]
}

private typealias Output = Long

private val solution = object : Solution<Input, Output>(2023, "Day08") {
  override fun parse(input: ProblemInput) = input.sections().let { (stepsSection, networkSection) ->
    val steps = stepsSection.charsAs(Step::parse)
    val network = Network.parse(networkSection)
    Input(steps, network)
  }

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output = input.countStepsToSink(input.network.source())

  override fun part2(input: Input): Output =
    input.network.ghostSources().map(input::countStepsToSink).reduce { acc, cnt -> acc lcm cnt }
}

fun main() = solution.run()
