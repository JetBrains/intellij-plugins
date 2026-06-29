// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.lsWidget

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItemsProvider
import com.intellij.prettierjs.PrettierLanguageServiceManager
import com.intellij.prettierjs.isPrettierFormattingAllowedFor

class PrettierWidgetItemsProvider : LanguageServiceWidgetItemsProvider() {
  override fun createWidgetItems(project: Project, currentFile: VirtualFile?): List<LanguageServiceWidgetItem> {
    val manager = PrettierLanguageServiceManager.getInstance(project)

    // In nested layouts several cached services may be ancestors of `currentFile`; mark only the single service that
    // would actually format it, so at most one item is "for current file".
    val currentFileServiceLocation = currentFile
      ?.takeIf { isPrettierFormattingAllowedFor(project, it) }
      ?.let { manager.findCachedServiceLocationFor(it) }

    return manager.jsLinterServices.map { (location, serviceInfo) ->
      PrettierWidgetItem(project, currentFile, location, serviceInfo,
                         isForCurrentFile = location == currentFileServiceLocation)
    }
  }

  override fun registerWidgetUpdaters(project: Project, widgetDisposable: Disposable, updateWidget: () -> Unit) {
    PrettierLanguageServiceManager.getInstance(project).addJsLinterManagerListener(updateWidget, widgetDisposable)
  }
}
