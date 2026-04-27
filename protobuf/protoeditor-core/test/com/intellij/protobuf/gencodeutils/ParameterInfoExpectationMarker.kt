package com.intellij.protobuf.gencodeutils

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue

/**
 * Expectation marker for testing callable parameter completion.
 *
 * Marker: `EXPECT-PARAM-INFO: <highlighted_param> [| <param1>, <param2>, ...]`
 *
 * See [ExpectationMarkerBase]
 */
class ParameterInfoExpectationMarker private constructor(
  textRange: TextRange,
  val expectedContains: Set<String>,
  val expectedHighlighted: String,
) : ExpectationMarkerBase(textRange) {

  fun checkParameterInfo(hintText: String, lineNumber: Int) {
    val highlightedName = Regex("<b>\\s*([\\w_]+).*?</b>").find(hintText)?.groupValues?.get(1)

    assertEquals(
      "Expected highlighted '$expectedHighlighted' at line $lineNumber.\nActual hint: $hintText",
      expectedHighlighted,
      highlightedName,
    )

    for (expectedName in expectedContains) {
      assertTrue(
        "Missing expected parameter '$expectedName' at line $lineNumber.\nActual hint: $hintText",
        hintText.contains(expectedName),
      )
    }
  }

  companion object {
    const val EXPECT_MARKER = "EXPECT-PARAM-INFO:"

    fun parseExpectations(file: PsiFile): List<ParameterInfoExpectationMarker> =
      parseFile(file, EXPECT_MARKER) { partitionRange, markerValue ->
        val parts = markerValue.split('|', limit = 2).map { it.trim() }
        require(parts.isNotEmpty() && parts[0].isNotEmpty())

        val contains = if (parts.size == 1) {
          emptySet()
        }
        else {
          parts[1]
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
        }

        ParameterInfoExpectationMarker(partitionRange, contains, parts[0])
      }
  }
}
