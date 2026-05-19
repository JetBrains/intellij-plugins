package com.intellij.lang.javascript.linter.eslint.lsWidget

import com.intellij.lang.javascript.linter.eslint.service.EslintLanguageServiceManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItemsProvider

class EsLintWidgetItemsProvider : LanguageServiceWidgetItemsProvider() {
  override fun createWidgetItems(project: Project, currentFile: VirtualFile?): List<LanguageServiceWidgetItem> =
    EslintLanguageServiceManager.getInstance(project).jsLinterServices.map {
      EsLintWidgetItem(project, currentFile, it.key, it.value)
    }

  override fun registerWidgetUpdaters(project: Project, widgetDisposable: Disposable, updateWidget: () -> Unit) {
    EslintLanguageServiceManager.getInstance(project).addJsLinterManagerListener(updateWidget, widgetDisposable)
  }
}
