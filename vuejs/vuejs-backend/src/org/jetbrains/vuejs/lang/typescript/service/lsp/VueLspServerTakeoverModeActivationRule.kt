// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.lsp.LspServerActivationRule
import com.intellij.lang.typescript.lsp.ServiceActivationHelper
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.vuejs.lang.html.isVueFile
import org.jetbrains.vuejs.lang.typescript.service.isVueServiceContext
import org.jetbrains.vuejs.options.VueLSMode
import org.jetbrains.vuejs.options.VueSettings

object VueLspServerTakeoverModeActivationRule : LspServerActivationRule(
  lspServerLoader = VueLspServerTakeoverModeLoader,
  activationRule = VueLspActivationHelper,
) {
  override fun isFileAcceptable(file: VirtualFile): Boolean {
    if (!TypeScriptLanguageServiceUtil.IS_VALID_FILE_FOR_SERVICE.value(file)) return false

    return file.isVueFile || TypeScriptLanguageServiceUtil.ACCEPTABLE_TS_FILE.value(file)
  }
}

private object VueLspActivationHelper : ServiceActivationHelper {
  override fun isProjectContext(project: Project, context: VirtualFile): Boolean {
    return isVueServiceContext(project, context)
  }

  override fun isEnabledInSettings(project: Project): Boolean {
    val settings = VueSettings.instance(project)
    return settings.serviceType == VueLSMode.MANUAL
           && settings.manualSettings.mode == VueSettings.ManualMode.ONLY_LSP_SERVER
  }
}