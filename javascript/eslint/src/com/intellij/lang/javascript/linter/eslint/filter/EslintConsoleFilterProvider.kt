package com.intellij.lang.javascript.linter.eslint.filter

import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir

internal class EslintConsoleFilterProvider : ConsoleFilterProvider {
  override fun getDefaultFilters(project: Project): Array<Filter> {
    return arrayOf(EslintErrorFilter(project, project.guessProjectDir()))
  }
}
