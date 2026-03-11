// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.lsp.LspServerActivationRule
import com.intellij.lang.typescript.lsp.ServiceActivationHelper
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.vuejs.lang.html.isVueFile
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.getAppropriateVueLSVersion
import org.jetbrains.vuejs.lang.typescript.service.isVueServiceContext
import org.jetbrains.vuejs.options.VueLSMode
import org.jetbrains.vuejs.options.VueSettings

class VueLspServerHybridModeActivationRule(
  runtime: VueServiceRuntime,
) : LspServerActivationRule(
  lspServerLoader = VueLspServerHybridModeLoaderFactory.getLoader(runtime),
  activationRule = ActivationHelper(runtime),
) {
  override fun isFileAcceptable(file: VirtualFile): Boolean {
    if (!TypeScriptLanguageServiceUtil.IS_VALID_FILE_FOR_SERVICE.test(file))
      return false

    return file.isVueFile || TypeScriptLanguageServiceUtil.ACCEPTABLE_TS_FILE.test(file)
  }
}

private class ActivationHelper(
  private val runtime: VueServiceRuntime,
) : ServiceActivationHelper {
  override fun isProjectContext(project: Project, context: VirtualFile): Boolean {
    val settings = VueSettings.instance(project)

    val runtimeMatchesSettings = when (runtime) {
      is VueServiceRuntime.Manual -> {
        settings.serviceType == VueLSMode.MANUAL
        && settings.manualSettings.mode == VueSettings.ManualMode.HYBRID_MODE
      }

      is VueServiceRuntime.Bundled -> {
        settings.serviceType == VueLSMode.AUTO
        && getAppropriateVueLSVersion(project, context) == runtime.version
      }
    }

    return isVueServiceContext(project, context) && runtimeMatchesSettings
  }

  override fun isEnabledInSettings(project: Project): Boolean {
    val settings = VueSettings.instance(project)
    return when (runtime) {
      is VueServiceRuntime.Manual ->
        settings.serviceType == VueLSMode.MANUAL
        && settings.manualSettings.mode == VueSettings.ManualMode.HYBRID_MODE

      is VueServiceRuntime.Bundled ->
        false // check that settings.serviceType == VueLSMode.AUTO when ready to enable hybrid mode in AUTO
    }
  }
}
