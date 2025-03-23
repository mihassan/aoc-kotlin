@file:Suppress("PackageDirectoryMismatch")

package aoc24.day24

import aoc24.day24.Gate.Companion.findGate
import aoc24.day24.Gate.Companion.fixOutputGates
import lib.Solution

/** A wire in the circuit denoted by its name. */
data class Wire(val name: String)

/** A group of wires with the same prefix id. */
data class WireGroup(val id: String, val wires: Set<Wire>) {
  /**
   * Get the value of the group of wires by checking signal for each wire and concatenating them.
   * The wires are sorted by name in descending order before concatenation, i.e., from the most
   * significant bit to the least significant bit.
   */
  fun getValue(signals: Map<Wire, Signal>): Long =
    wires
      .sortedBy { it.name }
      .reversed()
      .map { signals[it]?.value ?: error("No signal for wire $it") }
      .joinToString("").toLong(2)

  companion object {
    /** Group wires by their first character and create a map of wire groups. */
    fun groupWires(wires: Set<Wire>): Map<String, WireGroup> =
      wires
        .groupBy { it.name.first().toString() }
        .map { (id, wires) -> WireGroup(id, wires.toSet()) }
        .associateBy { it.id }
  }
}

/** A signal with a value of 0 or 1. A wire can carry a signal. */
data class Signal(val value: Long) {
  init {
    require(value in 0..1) { "Signal value must be 0 or 1" }
  }
}

/** Possible gate types with their corresponding operation. */
enum class GateType(val op: (Long, Long) -> Long) {
  AND(Long::and), OR(Long::or), XOR(Long::xor);

  companion object {
    fun parse(gateTypeStr: String): GateType = entries.find { it.name == gateTypeStr }
      ?: error("Invalid gate type: $gateTypeStr")
  }
}

/** A gate with two input wires and one output wire. */
data class Gate(val gateType: GateType, val input1: Wire, val input2: Wire, val output: Wire) {
  /** Compute the output signal of the gate based on the input signals. */
  fun computeOutputSignal(signals: Map<Wire, Signal>): Signal? {
    val signal1 = signals[input1]?.value ?: return null
    val signal2 = signals[input2]?.value ?: return null

    return Signal(gateType.op(signal1, signal2))
  }

  companion object {
    private val GATE_REGEX = """(\w+) (\w+) (\w+) -> (\w+)""".toRegex()

    fun parse(gateStr: String): Gate {
      val (input1Str, gateTypeStr, input2Str, outputStr) =
        GATE_REGEX.matchEntire(gateStr)?.destructured
          ?: error("Invalid gate: $gateStr")

      return Gate(
        gateType = GateType.parse(gateTypeStr),
        input1 = Wire(input1Str),
        input2 = Wire(input2Str),
        output = Wire(outputStr)
      )
    }

    /**
     * Find gates with the given [gateType] and [inputs].
     * If [gateType] is null, all gate types are considered.
     * If [inputs] is a single element, it is matched against either input1 or input2.
     * If [inputs] is two elements, they are matched against input1 and input2, in any order.
     */
    fun List<Gate>.findGate(gateType: GateType?, vararg inputs: String): List<Gate> =
      filter { gate ->
        (gateType == null || gate.gateType == gateType)
          && inputs.all { it == gate.input1.name || it == gate.input2.name }
      }

    fun List<Gate>.fixOutputGates(fixes: List<String>): List<Gate> {
      require(fixes.size == 2) { "Expected 2 output gate names to swap." }
      val (gate1, gate2) = fixes

      return map { gate ->
        when (gate.output.name) {
          gate1 -> gate.copy(output = Wire(gate2))
          gate2 -> gate.copy(output = Wire(gate1))
          else -> gate
        }
      }
    }
  }
}

/** A circuit with signals carried by input wires and all the gates. */
data class Circuit(val signals: Map<Wire, Signal>, val gates: List<Gate>) {
  /** Run the circuit to compute the signals for all the wires. */
  fun runSignals(): Map<Wire, Signal> {
    val signals = signals.toMutableMap()
    val gatesMap = gates.associateBy { it.output }

    fun dfs(gate: Gate) {
      if (gate.output in signals) return

      listOf(gate.input1, gate.input2)
        .filter { it !in signals }
        .forEach { dfs(gatesMap[it] ?: error("No gate produces signal for $it")) }

      signals[gate.output] =
        gate.computeOutputSignal(signals) ?: error("Cannot runSignals signal for $gate")
    }

    gates.forEach { dfs(it) }

    return signals
  }

  companion object {
    fun parse(circuitStr: String): Circuit {
      val (signalStrs, gateStrs) = circuitStr.split("\n\n")
      val signals = signalStrs.lines().associate { line ->
        val (wireStr, signalStr) = line.split(": ")
        val value = Signal(signalStr.toLong())
        val wire = Wire(wireStr)
        wire to value
      }
      val gates = gateStrs.lines().map { Gate.parse(it) }
      return Circuit(signals, gates)
    }
  }
}

/** A half adder with two input wires, a sum wire, and a carry wire. */
data class HalfAdder(val input1: Wire, val input2: Wire, val sum: Wire, val carry: Wire) {
  companion object {
    /** Find a half adder with the given inputs and output wires. */
    fun findHalfAdder(
      gates: List<Gate>,
      input1: String,
      input2: String,
    ): HalfAdder? {
      val sumGate = gates.findGate(GateType.XOR, input1, input2).firstOrNull() ?: return null
      val carryGate = gates.findGate(GateType.AND, input1, input2).firstOrNull() ?: return null

      return HalfAdder(Wire(input1), Wire(input2), sumGate.output, carryGate.output)
    }
  }
}

/** A full adder with two input wires, a carry-in wire, a sum wire, and a carry-out wire. */
data class FullAdder(
  val input1: Wire,
  val input2: Wire,
  val carryIn: Wire,
  val sum: Wire,
  val carryOut: Wire,
) {
  companion object {
    /** Find a full adder with the given inputs and output wires. */
    fun findFullAdder(
      gates: List<Gate>,
      input1: String,
      input2: String,
      carryIn: String,
      sum: String,
    ): FullAdder? {
      val halfAdder1 = HalfAdder.findHalfAdder(gates, input1, input2) ?: return null
      val halfAdder2 =
        HalfAdder.findHalfAdder(gates, halfAdder1.sum.name, carryIn) ?: return null
      if (halfAdder2.sum.name != sum) return null
      val carryOutGate =
        gates.findGate(GateType.OR, halfAdder1.carry.name, halfAdder2.carry.name).firstOrNull()
          ?: return null

      return FullAdder(
        Wire(input1),
        Wire(input2),
        Wire(carryIn),
        halfAdder2.sum,
        carryOutGate.output
      )
    }

    /**
     * Fix the full adder with the given inputs and output wires.
     * Return a list of wires to fix in order to make the full adder work.
     */
    fun fixFullAdder(
      gates: List<Gate>,
      input1: String,
      input2: String,
      carryIn: String,
      sum: String,
    ): List<String> {
      val halfAdder1 = HalfAdder.findHalfAdder(gates, input1, input2)
        ?: error("No full adder for $input1, $input2")

      val sumGate =
        gates.findGate(GateType.XOR, carryIn).firstOrNull() ?: error("No sum gate for $carryIn")
      val internalSumGate =
        if (sumGate.input1.name == carryIn) sumGate.input2.name else sumGate.input1.name

      if (internalSumGate != halfAdder1.sum.name) {
        return listOf(internalSumGate, halfAdder1.sum.name)
      }

      if (sumGate.output.name != sum) {
        return listOf(sumGate.output.name, sum)
      }

      return emptyList<String>()
    }
  }
}

typealias Input = Circuit

typealias Output = String

private val solution = object : Solution<Input, Output>(2024, "Day24") {
  override fun parse(input: String): Input = Circuit.parse(input)

  override fun format(output: Output): String = output

  override fun part1(input: Input): Output {
    val signals = input.runSignals()
    return WireGroup.groupWires(signals.keys)["z"]
      ?.getValue(signals)
      ?.toString()
      ?: error("No wire group for z")
  }

  override fun part2(input: Input): Output {
    val fixes = mutableListOf<String>()
    var gates: List<Gate> = input.gates

    val h0 =
      HalfAdder.findHalfAdder(gates, "x00", "y00") ?: error("No half adder for x00, y00")
    var f = FullAdder.findFullAdder(gates, "x01", "y01", h0.carry.name, "z01")
      ?: run {
        val fix = FullAdder.fixFullAdder(gates, "x01", "y01", h0.carry.name, "z01")
        fixes += fix

        gates = gates.fixOutputGates(fix)
        FullAdder.findFullAdder(gates, "x01", "y01", h0.carry.name, "z01")
          ?: error("Could not fix full adder for $fix")
      }

    val indices = WireGroup.groupWires(input.signals.keys)["x"]!!.wires.sortedBy { it.name }
      .map { it.name.drop(1).toInt() }

    indices.drop(2).forEach {
      val input1 = "x%02d".format(it)
      val input2 = "y%02d".format(it)
      val sum = "z%02d".format(it)
      val carryIn = f.carryOut.name

      f = FullAdder.findFullAdder(gates, input1, input2, carryIn, sum)
        ?: run {
          val fix = FullAdder.fixFullAdder(gates, input1, input2, carryIn, sum)
          fixes += fix

          gates = gates.fixOutputGates(fix)
          FullAdder.findFullAdder(gates, input1, input2, carryIn, sum)
            ?: error("Could not fix full adder for $fix")
        }
    }

    return fixes.sorted().joinToString(",")
  }
}

fun main() = solution.run()
