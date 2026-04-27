package com.intellij.protobuf.gencodeutils

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

/**
 * Expectation marker for testing type inference.
 *
 * Marker: `EXPECT-TYPE: <type>`
 *
 * See [ExpectationMarkerBase]
 */
class TypeExpectationMarker private constructor(
  textRange: TextRange,
  val expectedType: String,
) : ExpectationMarkerBase(textRange) {

  companion object {
    const val EXPECT_MARKER = "EXPECT-TYPE:"

    fun parseExpectations(file: PsiFile): List<TypeExpectationMarker> =
      parseFile(file, EXPECT_MARKER) { partitionRange, markerValue ->
        val expectedType = markerValue.trim()
        require(expectedType.isNotEmpty())

        TypeExpectationMarker(partitionRange, expectedType)
      }
  }
}
