package org.jetbrains.qodana.dart

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange

internal data class DartProblem(
  val code: String,
  val severity: String,
  val message: String,
  val correction: String?,
  val location: DartAnalyzeLocation?,
)

internal data class DartAnalyzeLocation(
  val file: String?,
  val offset: Int?,
  val length: Int?,
  val startLine: Int?,
  val startColumn: Int? = null,
  val endLine: Int? = null,
  val endColumn: Int? = null,
)

internal fun parseDartProblems(json: String): List<DartProblem> {
  val root = JsonParser.parseString(json).asJsonObject
  val problems = root.requiredArray("diagnostics")
  return problems.map { element ->
    val problem = element.asJsonObject
    DartProblem(
      problem.requiredString("code"),
      problem.string("severity").orEmpty(),
      problem.requiredString("problemMessage"),
      problem.string("correctionMessage"),
      problem.objectOrNull("location")?.toDartAnalyzeLocation(),
    )
  }
}

private fun JsonObject.toDartAnalyzeLocation(): DartAnalyzeLocation {
  val range = objectOrNull("range")
  val start = range?.objectOrNull("start")
  val end = range?.objectOrNull("end")
  val offset = start?.intOrNull("offset")
  val length = offset?.let { startOffset -> end?.intOrNull("offset")?.minus(startOffset) }
  return DartAnalyzeLocation(
    string("file"),
    offset,
    length,
    start?.intOrNull("line"),
    start?.intOrNull("column"),
    end?.intOrNull("line"),
    end?.intOrNull("column"),
  )
}

private fun JsonObject.requiredArray(name: String): JsonArray = get(name)?.takeIf { it.isJsonArray }?.asJsonArray
  ?: error("Missing '$name' array")

private fun JsonObject.requiredString(name: String): String = string(name) ?: error("Missing '$name' string")

private fun JsonObject.string(name: String): String? = get(name)?.takeUnless { it.isJsonNull }?.asString

private fun JsonObject.intOrNull(name: String): Int? = get(name)?.takeUnless { it.isJsonNull }?.asInt

private fun JsonObject.objectOrNull(name: String): JsonObject? = get(name)?.takeIf { it.isJsonObject }?.asJsonObject

internal fun DartAnalyzeLocation.rangeIn(textLength: Int): TextRange {
  val start = offset?.coerceIn(0, textLength) ?: 0
  val end = start + (length ?: 0).coerceAtLeast(0).coerceAtMost(textLength - start)
  return TextRange(start, end)
}

internal fun DartAnalyzeLocation.rangeIn(document: Document): TextRange {
  val start = document.offsetAt(startLine, startColumn)
  val end = document.offsetAt(endLine, endColumn)
  if (start != null && end != null && start <= end) return TextRange(start, end)
  return rangeIn(document.textLength)
}

private fun Document.offsetAt(line: Int?, column: Int?): Int? {
  val lineIndex = line?.minus(1) ?: return null
  val columnIndex = column?.minus(1) ?: return null
  if (lineIndex !in 0 until lineCount || columnIndex < 0) return null
  val lineStartOffset = getLineStartOffset(lineIndex)
  return (lineStartOffset + columnIndex).takeIf { it <= getLineEndOffset(lineIndex) }
}
