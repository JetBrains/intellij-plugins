// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.lsWidget

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItemsProvider
import com.intellij.prettierjs.PrettierLanguageServiceManager

class PrettierWidgetItemsProvider : LanguageServiceWidgetItemsProvider() {
  override fun createWidgetItems(project: Project, currentFile: VirtualFile?): List<LanguageServiceWidgetItem> =
    PrettierLanguageServiceManager.getInstance(project).jsLinterServices.map {
      PrettierWidgetItem(project, currentFile, it.key, it.value)
    }

  override fun registerWidgetUpdaters(project: Project, widgetDisposable: Disposable, updateWidget: () -> Unit) {
    PrettierLanguageServiceManager.getInstance(project).addJsLinterManagerListener(updateWidget, widgetDisposable)
  }
}