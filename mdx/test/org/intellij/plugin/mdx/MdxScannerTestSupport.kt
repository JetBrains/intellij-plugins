package org.intellij.plugin.mdx

internal class CountingCharSequence private constructor(
  private val text: String,
  private val start: Int,
  private val end: Int,
  private val counter: Counter,
) : CharSequence {
  constructor(text: String) : this(text, 0, text.length, Counter())

  val accesses: Long
    get() = counter.accesses

  override val length: Int
    get() = end - start

  override fun get(index: Int): Char {
    counter.accesses++
    return text[start + index]
  }

  override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
    return CountingCharSequence(text, start + startIndex, start + endIndex, counter)
  }

  override fun toString(): String {
    counter.accesses += length
    return text.substring(start, end)
  }

  private class Counter(var accesses: Long = 0)
}

internal class CancellingCharSequence private constructor(
  private val text: String,
  private val start: Int,
  private val end: Int,
  private val counter: CancellationCounter,
) : CharSequence {
  constructor(text: String, cancellationThreshold: Long, cancel: () -> Unit) :
    this(text, 0, text.length, CancellationCounter(cancellationThreshold, cancel))

  val accesses: Long
    get() = counter.accesses

  override val length: Int
    get() = end - start

  override fun get(index: Int): Char {
    counter.recordAccesses(1)
    return text[start + index]
  }

  override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
    return CancellingCharSequence(text, start + startIndex, start + endIndex, counter)
  }

  override fun toString(): String {
    counter.recordAccesses(length.toLong())
    return text.substring(start, end)
  }

  private class CancellationCounter(
    private val cancellationThreshold: Long,
    private val cancel: () -> Unit,
  ) {
    var accesses: Long = 0
      private set
    private var cancellationRequested = false

    fun recordAccesses(count: Long) {
      accesses += count
      if (!cancellationRequested && accesses >= cancellationThreshold) {
        cancellationRequested = true
        cancel()
      }
    }
  }
}

internal class PrefixGuardCharSequence private constructor(
  private val text: String,
  private val start: Int,
  private val end: Int,
  private val guard: Guard,
) : CharSequence {
  constructor(text: String) : this(text, 0, text.length, Guard())

  override val length: Int
    get() = end - start

  fun expose(endOffset: Int) {
    require(endOffset in guard.visibleEnd..text.length)
    guard.visibleEnd = endOffset
  }

  override fun get(index: Int): Char {
    val absoluteIndex = start + index
    check(absoluteIndex < guard.visibleEnd) {
      "Read source offset $absoluteIndex beyond exposed prefix ${guard.visibleEnd}"
    }
    return text[absoluteIndex]
  }

  override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
    return PrefixGuardCharSequence(text, start + startIndex, start + endIndex, guard)
  }

  override fun toString(): String {
    check(end <= guard.visibleEnd) { "Read source through $end beyond exposed prefix ${guard.visibleEnd}" }
    return text.substring(start, end)
  }

  private class Guard(var visibleEnd: Int = 0)
}

internal fun lineEnds(text: String): List<Int> {
  return buildList {
    var lineEnd = text.indexOf('\n')
    while (lineEnd != -1) {
      add(lineEnd + 1)
      lineEnd = text.indexOf('\n', lineEnd + 1)
    }
    if (lastOrNull() != text.length) add(text.length)
  }
}

internal fun buildEsm(lines: Int): String {
  return buildString {
    append("export const values = [\n")
    repeat(lines) {
      append("  'value-")
      append(it)
      append("',\n")
    }
    append(']')
  }
}

internal fun buildJsx(elements: Int): String {
  return buildString {
    append("<Box>\n")
    repeat(elements) {
      append("  <Item value={")
      append(it)
      append("} />\n")
    }
    append("</Box>")
  }
}
