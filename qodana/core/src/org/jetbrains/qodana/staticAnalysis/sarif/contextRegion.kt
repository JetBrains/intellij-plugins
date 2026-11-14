package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.lang.Language
import com.jetbrains.qodana.sarif.model.ArtifactContent
import com.jetbrains.qodana.sarif.model.Region
import org.jetbrains.qodana.staticAnalysis.qodanaEnv
import java.lang.Integer.max
import kotlin.math.min

internal const val CONTEXT_MAX_LINES_MARGIN = 2
internal const val MAX_CONTEXT_CHARS_LENGTH = 1800

class Border(offset: Int, line: Int, val text: String, private val lineBorder: IntRange) {
  var currentOffset = offset
  private var currentLine = line

  fun moveLeft(): Boolean {
    val nextOffset = currentOffset - 1
    if (nextOffset < 0) return false
    if (text[nextOffset] == '\n') {
      if (!lineBorder.contains(currentLine - 1)) return false
      currentLine--
    }
    currentOffset = nextOffset
    return true
  }

  fun moveRight(): Boolean {
    val nextOffset = currentOffset + 1
    if (nextOffset < 0 || nextOffset > text.length) return false
    if (text[currentOffset] == '\n') {
      if (!lineBorder.contains(currentLine + 1)) return false
      currentLine++
    }
    currentOffset = nextOffset
    return true
  }
}

fun getContextRegion(problem: CommonDescriptor,
                              text: String,
                              linesMargin: Int = CONTEXT_MAX_LINES_MARGIN,
                              fileLanguage: Language? = null): Region? =
  getContextRegion(problem, FileText(text), linesMargin, fileLanguage)

fun getContextRegion(problem: CommonDescriptor,
                     fileText: FileText,
                     linesMargin: Int = CONTEXT_MAX_LINES_MARGIN,
                     fileLanguage: Language? = null): Region? {
  if (qodanaEnv().QODANA_DISABLE_COLLECT_CONTEXT.value != null) return null

  val text = fileText.text
  val offset = getProblemOffset(fileText, problem) ?: return null
  val line = problem.line?.let { it - 1 } ?: return null
  if (problem.length == null) return null
  val leftBorder = Border(offset, line, text, IntRange(line - linesMargin, line + linesMargin))

  val rightBorderLine = fileText.offsetToLineColumn(offset + problem.length)?.line ?: return null

  //non including border symbol itself
  val rightBorder = Border(offset + problem.length, rightBorderLine, text,
                           IntRange(rightBorderLine - linesMargin, rightBorderLine + linesMargin))

  var leftMoved = true
  var rightMoved = true
  while ((rightBorder.currentOffset - leftBorder.currentOffset) < MAX_CONTEXT_CHARS_LENGTH && (leftMoved || rightMoved)) {
    leftMoved = leftBorder.moveLeft()
    rightMoved = rightBorder.moveRight()
  }

  val leftOffset = max(0, leftBorder.currentOffset)
  val rightOffset = min(rightBorder.currentOffset, text.length)
  val lineColumn = fileText.offsetToLineColumn(leftOffset)!!

  val region = Region()
    .withStartColumn(lineColumn.column + 1)
    .withStartLine(lineColumn.line + 1)
    .withCharLength(rightOffset - leftOffset)
    .withCharOffset(leftOffset)
    .withSnippet(ArtifactContent().withText(text.subSequence(leftOffset, rightOffset).toString()))

  if (fileLanguage != null) {
    region.withSourceLanguage(fileLanguage.id)
  }
  return region
}

internal fun getProblemOffset(text: String, problem: CommonDescriptor): Int? = getProblemOffset(FileText(text), problem)

internal fun getProblemOffset(fileText: FileText, problem: CommonDescriptor): Int? {
  val line = problem.line?.let { it - 1 } ?: return null
  val column = problem.column ?: return null
  assert(line >= 0)
  return fileText.lineColToOffset(line, column)
}