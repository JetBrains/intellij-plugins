// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.intellij.lang.typescript.lsp.JSFrameworkLspServerDescriptor
import com.intellij.lang.typescript.lsp.JSFrameworkLspServerSupportProvider
import com.intellij.lang.typescript.lsp.JSLspServerWidgetItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.lang.typescript.service.VueLspServerActivationRule
import org.jetbrains.vuejs.options.VueConfigurable

internal class VueLspServerSupportProvider :
  JSFrameworkLspServerSupportProvider(VueLspServerActivationRule) {
  override fun createLspServerDescriptor(project: Project): JSFrameworkLspServerDescriptor =
    VueLspServerDescriptor(project)

  override fun createLspServerWidgetItem(
    lspServer: LspServer,
    currentFile: VirtualFile?,
  ): LspServerWidgetItem =
    JSLspServerWidgetItem(
      lspServer = lspServer,
      currentFile = currentFile,
      itemIcon = VuejsIcons.Vue,
      statusBarIcon = VuejsIcons.Vue,
      settingsPageClass = VueConfigurable::class.java,
    )
}

internal class VueLspServerDescriptor(project: Project) : JSFrameworkLspServerDescriptor(
  project = project,
  activationRule = VueLspServerActivationRule,
  presentableName = "Vue",
) {
  override fun createInitializationOptionsWithTS(targetPath: String): Any {
    @Suppress("unused")
    return object {
      val typescript = object {
        val tsdk = targetPath
      }
      val vue = object {
        val hybridMode = false
      }
    }
  }
}
