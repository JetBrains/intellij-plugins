package org.intellij.plugin.mdx

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.testFramework.junit5.TestApplication
import org.intellij.plugin.mdx.lang.parse.MdxEsmScanner
import org.intellij.plugin.mdx.lang.parse.MdxExpressionBoundaryScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class MdxScannerPerformanceTest {
  @Test
  fun javaScriptScansStopAfterCancellation() {
    assertCancelsDuringScan("{[" + "value,".repeat(10_000) + "]}") { text ->
      MdxExpressionBoundaryScanner.findExpressionEnd(text, 0, text.length)
    }
    assertCancelsDuringScan(buildEsm(10_000)) { text ->
      MdxEsmScanner.scanBlock(text, 0)
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

  private companion object {
    private const val CANCELLATION_THRESHOLD = 2_048L
    private const val MAX_CANCELLATION_OVERHEAD = 4_096L
  }
}
