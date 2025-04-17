@file:Suppress("PackageDirectoryMismatch")

package aoc21.day18

import lib.Combinatorics.combinations
import lib.Parser
import lib.ParserCombinators.between
import lib.ParserCombinators.filter
import lib.ParserCombinators.map
import lib.ParserCombinators.or
import lib.ParserCombinators.sepBy
import lib.ParserCombinators.sepBy1
import lib.Parsers.char
import lib.Parsers.int
import lib.Parsers.newline
import lib.Parsers.recursiveParser
import lib.Solution

private sealed class Tree(var parent: Tree? = null) {
  fun copy(): Tree = when (this) {
    is Leaf -> Leaf(value)
    is Node -> Node(left.copy(), right.copy())
  }

  /**
   * A leaf node in the tree. It contains a value and a reference to its parent node.
   * This is intentionally not a data class to avoid deduping same value leaves.
   */
  class Leaf(var value: Int) : Tree() {
    /**
     * Finds the predecessor of this leaf node. A predecessor is defined as the rightmost leaf node
     * which comes before this leaf node in the tree while doing in-order traversal.
     */
    fun predecessor(): Leaf? {
      // Continue going up the tree until we find a node that is not the left child of its parent.
      var current: Tree = this
      var parent: Node? = this.parent as? Node
      while (parent?.left == current) {
        // If we are the left child, we need to go up to the parent.
        current = parent
        parent = parent.parent as? Node
      }

      if (parent == null) {
        // We are at the root node, so there is no predecessor.
        return null
      }

      // Now we are at a node that is not the left child of its parent. We need to find the
      // rightmost leaf node in the left subtree of this node.
      var rightmost: Tree? = parent.left
      while (rightmost is Node) {
        // Keep going to the right until we find a leaf node.
        rightmost = rightmost.right
      }
      return rightmost as? Leaf
    }

    /**
     * Finds the successor of this leaf node. A successor is defined as the leftmost leaf node
     * which comes after this leaf node in the tree while doing in-order traversal.
     */
    fun successor(): Leaf? {
      // Continue going up the tree until we find a node that is not the right child of its parent.
      var current: Tree = this
      var parent: Node? = this.parent as? Node
      while (parent?.right == current) {
        // If we are the right child, we need to go up to the parent.
        current = parent
        parent = parent.parent as? Node
      }

      if (parent == null) {
        // We are at the root node, so there is no successor.
        return null
      }

      // Now we are at a node that is not the right child of its parent. We need to find the
      // leftmost leaf node in the right subtree of this node.
      var leftmost: Tree? = parent.right
      while (leftmost is Node) {
        // Keep going to the left until we find a leaf node.
        leftmost = leftmost.left
      }
      return leftmost as? Leaf
    }
  }

  /**
   * A node in the tree. It contains references to its left and right children, which can be either
   * leaf nodes or other nodes. It also has a reference to its parent node.
   * This is intentionally not a data class to avoid deduping same value nodes.
   */
  class Node(var left: Tree, var right: Tree) : Tree()

  /**
   * Checks if this tree is a pair. A pair is defined as a node with two leaf children.
   */
  fun isPair(): Boolean = this is Node && left is Leaf && right is Leaf

  /**
   * Populate the parent references for all nodes in the tree.
   */
  fun populateParent() {
    fun dfs(tree: Tree, parent: Tree?) {
      tree.parent = parent
      if (tree is Node) {
        dfs(tree.left, tree)
        dfs(tree.right, tree)
      }
    }

    dfs(this, null)
  }

  /**
   * Calculates the magnitude of the tree.
   * The magnitude is defined as the sum of:
   * - 3 times the magnitude of the left child and
   * - 2 times the magnitude of the right child.
   */
  fun magnitude(): Long = when (this) {
    is Leaf -> value.toLong()
    is Node -> 3 * left.magnitude() + 2 * right.magnitude()
  }

  /**
   * Combines two trees into a new tree.
   * The new tree is a node with the two trees as its left and right children.
   * The parent references of the original trees are updated to point to the new node.
   */
  operator fun plus(other: Tree): Tree {
    val newNode = Node(this, other)
    this.parent = newNode
    other.parent = newNode
    return newNode
  }

  /**
   * Reduces the tree by applying the explode and split operations repeatedly.
   * The explode operation is applied first, followed by the split operation.
   * The process continues until no more reductions can be made.
   */
  fun reduce() {
    while (true) {
      if (explode()) {
        continue
      }
      if (split()) {
        continue
      }
      break
    }
  }

  /**
   * Explodes the tree if it is a pair and its depth is greater than or equal to 4.
   * The explode operation replaces the pair with a new leaf node with value 0.
   * The left and right values of the pair are added to the predecessor and successor leaves, respectively.
   */
  fun explode(depth: Int = 0): Boolean = when {
    this is Node && isPair() && depth >= 4 -> {
      // Add the left value to the predecessor leaf
      val leftLeaf = this.left as Leaf
      val leftPredecessor = leftLeaf.predecessor()
      if (leftPredecessor != null) {
        leftPredecessor.value += leftLeaf.value
      }
      // Add the right value to the successor leaf
      val rightLeaf = this.right as Leaf
      val rightSuccessor = rightLeaf.successor()
      if (rightSuccessor != null) {
        rightSuccessor.value += rightLeaf.value
      }
      // Set the current node to a new leaf with value 0
      val parent = this.parent as Node
      val newLeaf = Leaf(0)
      newLeaf.parent = parent
      if (parent.left == this) {
        parent.left = newLeaf
      } else {
        parent.right = newLeaf
      }
      true
    }

    this is Node -> left.explode(depth + 1) || right.explode(depth + 1)
    else -> false
  }

  /**
   * Splits the tree if it is a leaf with a value greater than or equal to 10.
   * The split operation replaces the leaf with a new node containing two new leaves.
   */
  fun split(): Boolean {
    return when {
      this is Leaf && value >= 10 -> {
        // Split the leaf into two new leaves and create a new node
        val leftLeaf = Leaf(value / 2)
        val rightLeaf = Leaf(value - value / 2)
        val newNode = Node(leftLeaf, rightLeaf)
        leftLeaf.parent = newNode
        rightLeaf.parent = newNode

        // Set the parent of the new node to the current node's parent
        val parent = this.parent as Node
        newNode.parent = parent
        // Place the new node in the correct position in the tree
        if (parent.left == this) {
          parent.left = newNode
        } else {
          parent.right = newNode
        }
        return true
      }

      this is Node -> return left.split() || right.split()
      else -> false
    }
  }
}

/**
 * Parses a tree structure from a string representation.
 * Using [or] to combine parsers for leaf and node does not quite work due to circular references.
 * So, we need to wrap the node parser in a [recursiveParser] to allow for recursive parsing.
 */
private val treeParser: Parser<Tree> = recursiveParser {
  nodeParser or leafParser
}

private val treeParsers: Parser<List<Tree>> = treeParser sepBy newline

private val leafParser: Parser<Tree.Leaf> = int.map { Tree.Leaf(it) }

private val nodeParser: Parser<Tree.Node> =
  treeParser sepBy1 char(',') between (char('[') to char(']')) filter { it.size == 2 } map {
    Tree.Node(it[0], it[1])
  }

private typealias Input = List<Tree>
private typealias Output = Long

private val solution = object : Solution<Input, Output>(2021, "Day18") {
  override fun parse(input: String): Input =
    treeParsers.parse(input)?.value ?: error("Invalid input")

  override fun format(output: Output): String = "$output"

  override fun part1(input: Input): Output {
    val trees = input.map {
      it.copy().apply {
        populateParent()
      }
    }
    val result = trees.reduce { x, y ->
      val xy = x + y
      xy.reduce()
      xy
    }
    return result.magnitude()
  }

  override fun part2(input: Input): Output =
    combinations(input.indices.toSet(), 2)
      .flatMap { indices ->
        val (i, j) = indices.min() to indices.max()
        listOf(i to j, j to i)
      }
      .maxOf { (i, j) ->
        val x = input[i].copy()
        val y = input[j].copy()
        val xy = x + y

        xy.populateParent()
        xy.reduce()
        xy.magnitude()
      }
}

fun main() = solution.run()
