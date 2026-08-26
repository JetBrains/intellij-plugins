package org.intellij.plugin.mdx

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.testFramework.junit5.TestApplication
import org.intellij.markdown.MarkdownElementType
import org.intellij.markdown.parser.CancellationToken
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.plugin.mdx.lang.parse.MdxEsmScanner
import org.intellij.plugin.mdx.lang.parse.MdxExpressionBoundaryScanner
import org.intellij.plugin.mdx.lang.parse.MdxFlavourDescriptor
import org.intellij.plugin.mdx.lang.parse.MdxJsxScanner
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownCodeSpanScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class MdxScannerPerformanceTest {
  @Test
  fun scannerLoopsStopAfterCancellation() {
    assertCancelsDuringScan("{[" + "value,".repeat(10_000) + "]}") { text ->
      MdxExpressionBoundaryScanner.findExpressionEnd(text, 0, text.length)
    }
    assertCancelsDuringScan(buildEsm(10_000)) { text ->
      MdxEsmScanner.scanBlock(text, 0)
    }
    assertCancelsDuringScan(buildJsx(10_000)) { text ->
      MdxJsxScanner.scanJsxElement(text, 0)
    }
    assertCancelsDuringScan("before ```code``` ".repeat(10_000)) { text ->
      MdxMarkdownCodeSpanScanner.Session(text, 0).advanceTo(text.length)
    }
  }

  @Test
  fun javaScriptScansCheckCancellationBeforeReadingInput() {
    val indicator = ProgressIndicatorBase().also { it.cancel() }
    val text = CountingCharSequence("{value}")

    assertThrows(ProcessCanceledException::class.java) {
      ProgressManager.getInstance().runProcess(
        { MdxExpressionBoundaryScanner.findExpressionEnd(text, 0, text.length) },
        indicator,
      )
    }
    assertEquals(0, text.accesses)
  }

  @Test
  fun lexerTokenScanStopsPromptlyAfterCancellation() {
    val source = "{\"" + "value".repeat(100_000) + "\"}"
    val indicator = ProgressIndicatorBase()
    val text = CancellingCharSequence(source, CANCELLATION_THRESHOLD, indicator::cancel)

    assertThrows(ProcessCanceledException::class.java) {
      ProgressManager.getInstance().runProcess(
        { MdxExpressionBoundaryScanner.findExpressionEnd(text, 0, text.length) },
        indicator,
      )
    }
    assertTrue(
      text.accesses <= CANCELLATION_THRESHOLD + MAX_CANCELLATION_OVERHEAD,
      "Scanner read ${text.accesses} characters after cancellation threshold $CANCELLATION_THRESHOLD",
    )
  }

  @Test
  fun incrementalSessionsScaleLinearly() {
    assertLinearGrowth("expression", ::expressionAccesses)
    assertLinearGrowth("ESM", ::esmAccesses)
    assertLinearGrowth("JSX", ::jsxAccesses)
  }

  @Test
  fun flowJsxBlockParsingScalesLinearly() {
    assertLinearGrowth("flow JSX parsing", ::flowJsxParsingAccesses)
  }

  @Test
  fun inlineJsxParsingScalesLinearly() {
    assertLinearGrowth("inline JSX parsing", ::inlineJsxParsingAccesses)
    assertLinearGrowth("mixed inline parsing", ::mixedInlineParsingAccesses)
  }

  @Test
  fun largeJsxElementHasNoArbitraryScanLimit() {
    val text = buildJsx(18_000)
    assertTrue(text.length > 250_000)

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertEquals(MdxJsxScanner.Termination.MATCHED, element?.termination)
    assertEquals(text.length, element?.range?.last)
  }

  @Test
  fun denseCodeSpansAreIndexedLinearly() {
    val source = buildString {
      repeat(2_000) { index ->
        append("text ```code-")
        append(index)
        append("``` ")
      }
    }
    val text = CountingCharSequence(source)

    val spans = MdxMarkdownCodeSpanScanner.Session(text, 0).advanceTo(text.length)

    assertEquals(2_000, spans.size)
    assertTrue(text.accesses <= source.length * 12L, "${text.accesses} accesses for ${source.length} characters")
  }

  @Test
  fun unmatchedCodeSpanDelimitersAreIndexedLinearly() {
    val source = buildString {
      for (length in 1..300) {
        append('x')
        repeat(length) { append('`') }
        append(' ')
      }
    }
    val text = CountingCharSequence(source)

    val spans = MdxMarkdownCodeSpanScanner.Session(text, 0).advanceTo(text.length)

    assertEquals(emptyList<IntRange>(), spans)
    assertTrue(text.accesses <= source.length * 12L, "${text.accesses} accesses for ${source.length} characters")
  }

  private fun assertLinearGrowth(name: String, accessCounter: (Int) -> Long) {
    val small = accessCounter(600)
    val large = accessCounter(1_200)
    assertTrue(large <= small * 5 / 2, "$name character accesses grew from $small to $large")
  }

  private fun assertCancelsDuringScan(source: String, scan: (CharSequence) -> Unit) {
    val indicator = ProgressIndicatorBase()
    val text = CancellingCharSequence(source, CANCELLATION_THRESHOLD, indicator::cancel)

    assertThrows(ProcessCanceledException::class.java) {
      ProgressManager.getInstance().runProcess({ scan(text) }, indicator)
    }
    assertTrue(text.accesses >= CANCELLATION_THRESHOLD)
  }

  private fun expressionAccesses(lines: Int): Long {
    val text = buildString {
      append("{[\n")
      repeat(lines) {
        append("  { value: ")
        append(it)
        append(" },\n")
      }
      append("]\n}")
    }
    val countingText = CountingCharSequence(text)
    val session = MdxExpressionBoundaryScanner.Session(countingText, 0)
    for (limit in lineEnds(text)) {
      session.advanceTo(limit)
    }
    return countingText.accesses
  }

  private fun esmAccesses(lines: Int): Long {
    val text = buildEsm(lines)
    val countingText = CountingCharSequence(text)
    val session = MdxEsmScanner.Session(countingText, 0)
    for (limit in lineEnds(text)) {
      session.advanceTo(limit)
    }
    return countingText.accesses
  }

  private fun jsxAccesses(elements: Int): Long {
    val text = buildJsx(elements)
    val countingText = CountingCharSequence(text)
    val session = MdxJsxScanner.Session(countingText, 0)
    for (limit in lineEnds(text)) {
      session.advanceTo(limit)
    }
    return countingText.accesses
  }

  private fun flowJsxParsingAccesses(elements: Int): Long {
    return markdownParsingAccesses(buildJsx(elements))
  }

  private fun inlineJsxParsingAccesses(elements: Int): Long {
    val text = buildString {
      append("before ")
      repeat(elements) {
        append("<Item value={")
        append(it)
        append("}>text</Item> ")
      }
      append("after")
    }
    return markdownParsingAccesses(text)
  }

  private fun mixedInlineParsingAccesses(elements: Int): Long {
    val text = buildString {
      append("prefix ")
      repeat(elements) {
        append("*before <Item data={{ value: ")
        append(it)
        append(" }}>`code-")
        append(it)
        append("` {")
        append(it)
        append("}</Item> after* ")
      }
      append("suffix")
    }
    return markdownParsingAccesses(text)
  }

  private fun markdownParsingAccesses(text: String): Long {
    val countingText = CountingCharSequence(text)
    MarkdownParser(MdxFlavourDescriptor, cancellationToken = CancellationToken.NonCancellable)
      .parse(MarkdownElementType("MDX_PERFORMANCE_TEST_ROOT"), countingText)
    return countingText.accesses
  }

  private companion object {
    private const val CANCELLATION_THRESHOLD = 2_048L
    private const val MAX_CANCELLATION_OVERHEAD = 4_096L
  }
}
