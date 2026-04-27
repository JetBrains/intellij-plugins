package com.intellij.protobuf.gencodeutils

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.testFramework.UsefulTestCase.assertContainsElements

/**
 * Expectation marker for testing autocompletion.
 *
 * Marker: `EXPECT-COMPLETE: <item1>, <item2>, ...`
 *
 * See [ExpectationMarkerBase]
 */
class CompletionExpectationMarker private constructor(
  textRange: TextRange,
  val expectedVariants: Set<String>,
) : ExpectationMarkerBase(textRange) {

  fun checkCompletions(actualVariants: Collection<String>, lineNumber: Int) {
    assertContainsElements(
      "Missing expected completions at line $lineNumber",
      actualVariants,
      expectedVariants,
    )
  }

  companion object {
    const val EXPECT_MARKER = "EXPECT-COMPLETE:"

    fun parseExpectations(file: PsiFile): List<CompletionExpectationMarker> =
      parseFile(file, EXPECT_MARKER) { partitionRange, markerValue ->
        val variants = markerValue
          .split(',')
          .map { it.trim() }
          .filter { it.isNotEmpty() }
          .toSet()

        require(variants.isNotEmpty())

        CompletionExpectationMarker(partitionRange, variants)
      }
  }
}
