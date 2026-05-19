package com.intellij.lang.javascript.linter.eslint.filter

import com.intellij.execution.filters.AbstractFileHyperlinkFilter
import com.intellij.execution.filters.FileHyperlinkRawData
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class EslintErrorFilter(project: Project, baseDir: VirtualFile?) : AbstractFileHyperlinkFilter(project, baseDir), DumbAware {
  @Volatile
  private var stateHistory: StateHistory? = null

  override fun parse(line: String): List<FileHyperlinkRawData> {
    val nextState = advance(line, stateHistory)
    stateHistory = nextState
    return nextState.lastState?.links ?: emptyList()
  }

  private fun advance(line: String, stateHistory: StateHistory?): StateHistory {
    val prevState = stateHistory?.let {
      if (line.startsWith(it.lastLine)) it.previousState else it.lastState
    }
    return StateHistory(FINDER.nextState(line, prevState), line, prevState)
  }

  companion object {
    private val FINDER = EslintErrorLinkFinder()
  }

  internal class State(val filePath: String, val links: List<FileHyperlinkRawData>)

  private class StateHistory(val lastState: State?, val lastLine: String, val previousState: State?)
}