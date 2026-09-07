package org.intellij.plugin.mdx.lang.parse

import com.intellij.openapi.util.TextRange

internal object MdxJsxScanner {
  enum class TagKind {
    OPENING,
    CLOSING,
    SELF_CLOSING
  }

  enum class Termination {
    MATCHED,
    RECOVERED,
    UNTERMINATED,
  }

  data class Tag(
    val range: TextRange,
    val kind: TagKind,
    val name: String?,
    val attributes: List<TextRange>,
    val expressions: List<TextRange>,
  )

  data class Element(
    val range: TextRange,
    val tags: List<Tag>,
    val expressions: List<TextRange>,
    val incomplete: Boolean,
    val termination: Termination,
  )

  data class ElementIdentity(val name: String?)

  internal class Session(
    source: CharSequence,
    private val start: Int,
    private val scanEnd: Int = source.length,
    private val baseOpaqueRanges: MdxOpaqueRangeLookup = MdxTextRangeSet.EMPTY,
  ) {
    private val text = mdxCancellableText(source)
    private val addedOpaqueRanges = MdxMutableTextRangeSet()
    private val allOpaqueRanges = MdxOpaqueRangeLookup(::endOffsetContaining)
    private var recoveryBudget = MdxExpressionBoundaryScanner.RecoveryBudget()
    private val tags = RetainedAccumulator<Tag>()
    private val expressions = RetainedAccumulator<TextRange>()
    private val nesting = NestingState()
    private var cursor = start
    private var exposedEnd = start
    private var activeTag: MdxJsxTagParser? = null
    private var activeExpression: ExpressionSession? = null
    private var invalidRoot = text.getOrNull(start) != '<'
    private var termination: Termination? = null
    private var previousAdvanceStart = start
    private var previousAdvanceSnapshot: Snapshot? = snapshot()
    private var lastResult: Element? = null
    private var resultIsCurrent = false

    fun advanceTo(limit: Int): Element? {
      mdxCancellableText(text)
      require(limit in exposedEnd..scanEnd) {
        "JSX scan limit must advance from $exposedEnd to at most $scanEnd: $limit"
      }
      if (limit == exposedEnd && resultIsCurrent) return lastResult
      if (limit > exposedEnd) {
        commitPreviousAdvance()
        previousAdvanceStart = cursor
        previousAdvanceSnapshot = snapshot()
        exposedEnd = limit
      }
      return scanTo(limit).also {
        lastResult = it
        resultIsCurrent = true
      }
    }

    private fun scanTo(limit: Int): Element? {
      while (cursor < limit && termination == null && !invalidRoot) {
        val expression = activeExpression
        if (expression != null) {
          val boundary = expression.session.advanceToBoundary(limit)
          if (boundary.end == -1) return elementAt(limit)
          if (!boundary.stable) return scanProvisionalExpression(expression, boundary.end, limit)
          expressions.add(TextRange(expression.start, boundary.end))
          cursor = boundary.end
          activeExpression = null
          continue
        }

        val tagSession = activeTag
        if (tagSession != null) {
          when (val result = tagSession.advanceTo(limit)) {
            MdxJsxTagParser.Result.Pending -> return elementAt(limit)
            MdxJsxTagParser.Result.Invalid -> {
              activeTag = null
              if (hasNoTags()) {
                invalidRoot = true
                return null
              }
              cursor = tagSession.start + 1
              continue
            }
            is MdxJsxTagParser.Result.Complete -> {
              activeTag = null
              cursor = result.tag.range.endOffset
              accept(result.tag)
              continue
            }
          }
        }

        val opaqueEnd = allOpaqueRanges.endOffsetContaining(cursor)
        if (opaqueEnd != null) {
          cursor = opaqueEnd.coerceAtMost(limit)
          continue
        }
        when (text[cursor]) {
          '{' -> activeExpression = ExpressionSession(
            cursor,
            MdxExpressionBoundaryScanner.Session(text, cursor, scanEnd),
          )
          '<' -> activeTag = MdxJsxTagParser(text, cursor, scanEnd, recoveryBudget)
          else -> cursor++
        }
      }
      return elementAt(limit)
    }

    private fun scanProvisionalExpression(expression: ExpressionSession, expressionEnd: Int, limit: Int): Element? {
      val state = ProvisionalScanState(
        cursor,
        nesting.snapshot(),
        invalidRoot,
        termination,
        tags.checkpoint(),
        expressions.checkpoint(),
      )
      val durableRecoveryBudget = recoveryBudget
      recoveryBudget = durableRecoveryBudget.copy()
      expressions.add(TextRange(expression.start, expressionEnd))
      cursor = expressionEnd
      activeExpression = null
      return try {
        scanTo(limit)
      }
      finally {
        recoveryBudget = durableRecoveryBudget
        cursor = state.cursor
        nesting.restore(state.nesting)
        invalidRoot = state.invalidRoot
        termination = state.termination
        tags.restore(state.tagCheckpoint)
        expressions.restore(state.expressionCheckpoint)
        activeTag = null
        activeExpression = expression
      }
    }

    fun addOpaqueRanges(ranges: Collection<TextRange>) {
      if (ranges.none { !it.isEmpty }) return
      addedOpaqueRanges.addAll(ranges)

      val snapshot = previousAdvanceSnapshot
      if (snapshot != null && ranges.any { it.intersects(previousAdvanceStart, cursor) }) {
        restore(snapshot)
        resultIsCurrent = false
      }
    }

    fun opaqueRangeLookup(): MdxOpaqueRangeLookup = allOpaqueRanges

    private fun accept(tag: Tag) {
      if (hasNoTags() && tag.kind == TagKind.CLOSING) {
        invalidRoot = true
        return
      }
      tags.add(tag)
      expressions.addAll(tag.expressions)
      termination = nesting.accept(tag)
    }

    private fun elementAt(limit: Int): Element? {
      if (invalidRoot || hasNoTags()) return null
      val actualTermination = termination ?: Termination.UNTERMINATED
      return Element(
        TextRange(start, if (termination == null) limit else cursor),
        tags.snapshot(),
        expressions.snapshot(),
        incomplete = termination == null || nesting.incomplete,
        termination = actualTermination,
      )
    }

    private fun hasNoTags(): Boolean = tags.isEmpty

    private fun commitPreviousAdvance() {
      tags.commit()
      expressions.commit()
    }

    private fun endOffsetContaining(offset: Int): Int? {
      val baseEnd = baseOpaqueRanges.endOffsetContaining(offset)
      val addedEnd = addedOpaqueRanges.endOffsetContaining(offset)
      return when {
        baseEnd == null -> addedEnd
        addedEnd == null -> baseEnd
        else -> maxOf(baseEnd, addedEnd)
      }
    }

    private fun snapshot(): Snapshot? {
      if (activeTag != null || activeExpression != null) return null
      return Snapshot(
        cursor,
        nesting.snapshot(),
        invalidRoot,
        termination,
      )
    }

    private fun restore(snapshot: Snapshot) {
      cursor = snapshot.cursor
      nesting.restore(snapshot.nesting)
      invalidRoot = snapshot.invalidRoot
      termination = snapshot.termination
      tags.restore(0)
      expressions.restore(0)
      activeTag = null
      activeExpression = null
    }

    private data class Snapshot(
      val cursor: Int,
      val nesting: NestingSnapshot,
      val invalidRoot: Boolean,
      val termination: Termination?,
    )

    private data class ProvisionalScanState(
      val cursor: Int,
      val nesting: NestingSnapshot,
      val invalidRoot: Boolean,
      val termination: Termination?,
      val tagCheckpoint: Int,
      val expressionCheckpoint: Int,
    )

    private data class ExpressionSession(
      val start: Int,
      val session: MdxExpressionBoundaryScanner.Session,
    )
  }

  fun isLineStartJsx(text: CharSequence, start: Int): Boolean {
    val source = mdxCancellableText(text)
    val lineStart = mdxLineStart(source, start)
    val indent = mdxSmallIndent(source, lineStart, start)
    return indent != -1 &&
           lineStart + indent == start &&
           (MdxJsxTagParser.parse(source, start, source.length) != null ||
            isIncompleteOpeningTagStart(source, start, source.length))
  }

  /**
   * If a `<Name…` opening tag that has not yet been closed with `>` begins at [start] (see
   * [isIncompleteOpeningTagStart]), returns its range up to the logical end of a "clean" prefix — a
   * `<Name` with optional, fully-formed attributes, ending at a line break or end of input. A dangling
   * `{…` expression or unterminated quote (e.g. the static `<Broken attr={"unterminated}`) yields
   * `null` so it keeps its existing outer-language parse rather than being reinterpreted as JSX.
   * Used to project a freshly-typed `<My` into the MdxJS layer so the platform JSX tag-name completion
   * runs while the tag is still unbalanced. WEB-78468.
   */
  fun incompleteOpeningTagRange(text: CharSequence, start: Int, limit: Int = text.length): TextRange? {
    val source = mdxCancellableText(text)
    if (source.getOrNull(start) != '<') return null
    if (start + 1 >= limit || source[start + 1] == '\n' || source[start + 1] == '\r') {
      return TextRange(start, start + 1)
    }
    if (!isIncompleteOpeningTagStart(source, start, limit)) return null
    var offset = start + 1
    while (offset < limit && MdxJsxTagParser.isNamePart(source[offset])) {
      offset++
    }
    while (offset < limit) {
      when (source[offset]) {
        '\n' -> return TextRange(start, offset)
        '{' -> {
          val expressionEnd = MdxExpressionBoundaryScanner.findExpressionEnd(source, offset, limit)
          if (expressionEnd == -1) return null
          offset = expressionEnd
        }
        '\'', '"' -> {
          val quoteEnd = MdxJsxTagParser.scanQuoted(source, offset, limit, source[offset])
          if (quoteEnd == -1) return null
          offset = quoteEnd
        }
        else -> offset++
      }
    }
    return TextRange(start, limit)
  }

  fun scanJsxElement(
    text: CharSequence,
    start: Int,
    limit: Int = text.length,
    opaqueRanges: MdxTextRangeSet = MdxTextRangeSet.EMPTY,
  ): Element? {
    return Session(text, start, limit, opaqueRanges).advanceTo(limit)
  }

  fun openingElementIdentity(text: CharSequence, start: Int, limit: Int = text.length): ElementIdentity? {
    val tag = MdxJsxTagParser.parse(text, start, limit) ?: return null
    return if (tag.kind == TagKind.OPENING) ElementIdentity(tag.name) else null
  }

  fun closingElementIdentity(text: CharSequence, start: Int, limit: Int = text.length): ElementIdentity? {
    val tag = MdxJsxTagParser.parse(text, start, limit) ?: return null
    return if (tag.kind == TagKind.CLOSING) ElementIdentity(tag.name) else null
  }

  private fun isIncompleteOpeningTagStart(text: CharSequence, start: Int, limit: Int): Boolean {
    if (text.getOrNull(start) != '<' || start + 1 >= limit || text.getOrNull(start + 1) == '/') return false
    var offset = start + 1
    if (text.getOrNull(offset) == '>') return false
    if (!MdxJsxTagParser.isNameStart(text.getOrNull(offset))) return false
    offset++
    while (offset < limit && MdxJsxTagParser.isNamePart(text[offset])) {
      offset++
    }
    // A `>`/`<` outside an expression breaks the tag; one inside an unterminated `{...}` attribute
    // (e.g. an `=>` arrow) does not, so expressions are skipped as a unit rather than scanned char-by-char.
    while (offset < limit) {
      when (text[offset]) {
        '>', '<' -> return false
        '\n' -> return true
        '{' -> {
          val expressionEnd = MdxExpressionBoundaryScanner.findExpressionEnd(text, offset, limit)
          if (expressionEnd == -1) return true
          offset = expressionEnd
        }
        '\'', '"' -> {
          val quoteEnd = MdxJsxTagParser.scanQuoted(text, offset, limit, text[offset])
          if (quoteEnd == -1) return true
          offset = quoteEnd
        }
        else -> offset++
      }
    }
    return true
  }

  private class NestingState {
    private var topFrame: Frame? = null

    var incomplete: Boolean = false
      private set

    fun accept(tag: Tag): Termination? {
      when (tag.kind) {
        TagKind.OPENING -> topFrame = Frame(tag.name, topFrame)
        TagKind.SELF_CLOSING -> Unit
        TagKind.CLOSING -> {
          val current = topFrame
          if (current == null) {
            incomplete = true
            return Termination.RECOVERED
          }
          if (current.name == tag.name) {
            topFrame = current.parent
          }
          else {
            var ancestor = current.parent
            while (ancestor != null && ancestor.name != tag.name) {
              ancestor = ancestor.parent
            }
            if (ancestor == null) {
              incomplete = true
              return Termination.RECOVERED
            }
            incomplete = true
            topFrame = ancestor.parent
          }
        }
      }
      return if (topFrame == null) Termination.MATCHED else null
    }

    fun snapshot(): NestingSnapshot = NestingSnapshot(topFrame, incomplete)

    fun restore(snapshot: NestingSnapshot) {
      topFrame = snapshot.topFrame
      incomplete = snapshot.incomplete
    }
  }

  private class Frame(
    val name: String?,
    val parent: Frame?,
  )

  private data class NestingSnapshot(
    val topFrame: Frame?,
    val incomplete: Boolean,
  )

  /**
   * Shares an append-only committed prefix and owns the current advance's immutable suffix. Later
   * commits and rollbacks therefore cannot change a retained result.
   */
  private class RetainedAccumulator<E> {
    private val committed = mutableListOf<E>()
    private val provisional = mutableListOf<E>()

    val isEmpty: Boolean
      get() = committed.isEmpty() && provisional.isEmpty()

    fun add(value: E) {
      provisional.add(value)
    }

    fun addAll(values: Collection<E>) {
      provisional.addAll(values)
    }

    fun checkpoint(): Int = provisional.size

    fun restore(checkpoint: Int) {
      if (checkpoint < provisional.size) provisional.subList(checkpoint, provisional.size).clear()
    }

    fun commit() {
      committed.addAll(provisional)
      provisional.clear()
    }

    fun snapshot(): List<E> = RetainedList(committed, committed.size, provisional.toList())
  }

  private class RetainedList<E>(
    private val committed: List<E>,
    private val committedSize: Int,
    private val provisional: List<E>,
  ) : AbstractList<E>() {
    override val size: Int
      get() = committedSize + provisional.size

    override fun get(index: Int): E {
      if (index !in indices) throw IndexOutOfBoundsException("index: $index, size: $size")
      return if (index < committedSize) committed[index] else provisional[index - committedSize]
    }
  }

  private fun CharSequence.getOrNull(index: Int): Char? {
    return if (index in indices) this[index] else null
  }
}
