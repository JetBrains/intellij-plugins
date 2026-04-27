package com.intellij.protobuf.gencodeutils

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

/**
 * Base class for test expectation markers that partition test files.
 *
 * Each marker defines a partition starting from the next line and ending at the next marker (or EOF).
 * Expectations apply to all `<caret>` locations within this partition.
 *
 * Inheritors should define a static `parseExpectations(file: PsiFile)` function
 * that will parse the test file using [ExpectationMarkerBase.parseFile].
 */
abstract class ExpectationMarkerBase(@JvmField val textRange: TextRange) {
  companion object {
    /**
     * Parses expectations from a file into partitions using the provided marker.
     *
     * @param file The file to parse
     * @param marker The marker to search for
     * @param expectationFactory Function that creates expectation instances from partition
     *
     * @return List of parsed expectations
     */
    fun <T : ExpectationMarkerBase> parseFile(
      file: PsiFile,
      marker: String,
      expectationFactory: (partitionRange: TextRange, markerValue: String) -> T,
    ): List<T> {
      val text = file.text
      val document = file.viewProvider.document

      val expectations = mutableListOf<T>()

      var markerStart = text.indexOf(marker)
      while (markerStart != -1) {
        val lineNumber = document.getLineNumber(markerStart) + 1
        val markerEnd = markerStart + marker.length
        val lineEnd = text.indexOf('\n', markerEnd).let { if (it == -1) text.length else it }

        val markerValue = text.substring(markerEnd, lineEnd).trim()

        val partitionStart = if (lineEnd < text.length) lineEnd + 1 else text.length
        val nextMarkerStart = text.indexOf(marker, lineEnd)
        val partitionEnd = if (nextMarkerStart == -1) text.length else nextMarkerStart

        val expectation = try {
          expectationFactory(TextRange(partitionStart, partitionEnd), markerValue)
        }
        catch (e: Exception) {
          throw IllegalArgumentException(
            "Failed to parse '$marker' marker in file '${file.name}' at line $lineNumber: ${e.message}",
            e,
          )
        }
        expectations.add(expectation)

        markerStart = nextMarkerStart
      }

      return expectations
    }
  }
}
