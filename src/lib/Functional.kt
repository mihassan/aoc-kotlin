package lib

object Functional {
  fun <R> Boolean.whenTrue(block: () -> Unit): Unit = if (this) block() else Unit
  fun <R> Boolean.whenFalse(block: () -> Unit): Unit = if (!this) block() else Unit
}
