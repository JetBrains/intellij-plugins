// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.intellij.lang.typescript.lsp.JSFrameworkLspClientDescriptor
import com.intellij.lang.typescript.lsp.JSFrameworkLspIntegrationProvider
import com.intellij.lang.typescript.lsp.JSLspClientWidgetItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.lsWidget.LspClientWidgetItem
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.options.VueConfigurable

internal class VueLspIntegrationTakeoverModeProvider :
  JSFrameworkLspIntegrationProvider(VueLspServerTakeoverModeActivationRule) {
  override fun createLspServerDescriptor(project: Project): JSFrameworkLspClientDescriptor =
    VueLspClientTakeoverModeDescriptor(project)

  override fun createWidgetItem(
    lspClient: LspClient,
    currentFile: VirtualFile?,
  ): LspClientWidgetItem =
    JSLspClientWidgetItem(
      lspClient = lspClient,
      currentFile = currentFile,
      itemIcon = VuejsIcons.Vue,
      statusBarIcon = VuejsIcons.Vue,
      settingsPageClass = VueConfigurable::class.java,
    )
}
