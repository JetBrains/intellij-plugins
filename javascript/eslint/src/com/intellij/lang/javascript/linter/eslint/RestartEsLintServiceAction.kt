package com.intellij.lang.javascript.linter.eslint

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.lang.javascript.linter.eslint.service.EslintLanguageServiceManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class RestartEsLintServiceAction : AnAction(), DumbAware {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val project = e.project
    e.presentation.isEnabledAndVisible =
      project != null &&
      EslintLanguageServiceManager.getInstance(project).jsLinterServices.isNotEmpty()
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    EslintLanguageServiceManager.getInstance(project).terminateServices()

    // Restarting DaemonCodeAnalyzer will cause the ESLint service to start if needed
    DaemonCodeAnalyzer.getInstance(project).restart(this)
  }
}
