package com.intellij.lang.javascript.linter.eslint.filter

import com.intellij.execution.filters.FileHyperlinkRawData
import com.intellij.execution.filters.FileHyperlinkRawDataFinder
import com.intellij.execution.filters.PatternBasedFileHyperlinkRawDataFinder
import com.intellij.execution.filters.PatternHyperlinkFormat
import com.intellij.execution.filters.PatternHyperlinkPart
import com.intellij.openapi.util.io.OSAgnosticPathUtil
import java.util.regex.Pattern

internal class EslintErrorLinkFinder : FileHyperlinkRawDataFinder {

  fun nextState(line: String, prevState: EslintErrorFilter.State?): EslintErrorFilter.State? {
    if (OSAgnosticPathUtil.isAbsolute(line)) {
      return EslintErrorFilter.State(line.trim(), emptyList())
    }
    if (prevState != null) {
      if (OSAgnosticPathUtil.isAbsolute(prevState.filePath)) {
        createLinksState(LINE_FINDER_ABSOLUTE_PATH, line, prevState.filePath)?.let {
          return it
        }
      }
      createLinksState(LINE_FINDER_RELATIVE_PATH, line, prevState.filePath)?.let {
        return it
      }
    }
    if (line.isBlank()) {
      return null
    }
    return EslintErrorFilter.State(line.trim(), emptyList())
  }

  override fun find(line: String): List<FileHyperlinkRawData> {
    return emptyList()
  }

  private fun createLinksState(finder: FileHyperlinkRawDataFinder, line: String, filePath: String): EslintErrorFilter.State? {
    val links = finder.find(line)
    if (links.isEmpty()) {
      return null
    }
    return EslintErrorFilter.State(filePath, links.map {
      FileHyperlinkRawData(filePath, it.documentLine, it.documentColumn, it.hyperlinkStartInd, it.hyperlinkEndInd)
    })
  }

  companion object {
    private val LINE_FINDER_ABSOLUTE_PATH = PatternBasedFileHyperlinkRawDataFinder(arrayOf(
      PatternHyperlinkFormat(Pattern.compile("^\\s+((\\d+):(\\d+)\\s+(error|warning))\\s.*$"),
                             false, false,
                             PatternHyperlinkPart.HYPERLINK, PatternHyperlinkPart.LINE, PatternHyperlinkPart.COLUMN,
                             PatternHyperlinkPart.PATH)))
    private val LINE_FINDER_RELATIVE_PATH = PatternBasedFileHyperlinkRawDataFinder(arrayOf(
      PatternHyperlinkFormat(Pattern.compile("^\\s+(Line (\\d+):(\\d+))(:).*$"),
                             false, false,
                             PatternHyperlinkPart.HYPERLINK, PatternHyperlinkPart.LINE, PatternHyperlinkPart.COLUMN,
                             PatternHyperlinkPart.PATH)))
  }
}