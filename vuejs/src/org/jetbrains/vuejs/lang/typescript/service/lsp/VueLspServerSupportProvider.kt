// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.intellij.lang.typescript.lsp.JSFrameworkLspServerDescriptor
import com.intellij.lang.typescript.lsp.JSLspServerWidgetItem
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.lang.typescript.service.VueServiceSetActivationRule
import org.jetbrains.vuejs.lang.typescript.service.vueLspNewEvalVersion
import org.jetbrains.vuejs.options.VueConfigurable
import org.jetbrains.vuejs.options.VueSettings


class VueLspServerSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    if (VueServiceSetActivationRule.isLspServerEnabledAndAvailable(project, file)) {
      serverStarter.ensureServerStarted(VueLspServerDescriptor(project))
    }
  }

  override fun createLspServerWidgetItem(lspServer: LspServer, currentFile: VirtualFile?): LspServerWidgetItem =
    JSLspServerWidgetItem(lspServer, currentFile, VuejsIcons.Vue, VuejsIcons.Vue, VueConfigurable::class.java)
}

class VueLspServerDescriptor(project: Project) : JSFrameworkLspServerDescriptor(project, VueServiceSetActivationRule, "Vue") {
  val newEvalMode = project.service<VueSettings>().useTypesFromServer

  init {
    if (newEvalMode) {
      version = vueLspNewEvalVersion
    }
  }

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
