// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.intellij.lang.typescript.lsp.JSFrameworkLspClientDescriptor
import com.intellij.lang.typescript.lsp.JSFrameworkLspIntegrationProvider
import com.intellij.lang.typescript.lsp.JSLspClientWidgetItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.lsWidget.LspClientWidgetItem
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.options.VueConfigurable

internal class VueLspIntegrationHybridModeDefaultProvider :
  VueLspIntegrationHybridModeProvider(VueServiceRuntime.Bundled(VueLanguageToolsVersion.DEFAULT))

internal class VueLspIntegrationHybridModeLegacyProvider :
  VueLspIntegrationHybridModeProvider(VueServiceRuntime.Bundled(VueLanguageToolsVersion.LEGACY))

internal class VueLspIntegrationHybridModeManualProvider :
  VueLspIntegrationHybridModeProvider(VueServiceRuntime.Manual)

sealed class VueLspIntegrationHybridModeProvider(
  private val runtime: VueServiceRuntime,
) : JSFrameworkLspIntegrationProvider(VueLspServerHybridModeActivationRule(runtime)) {

  override fun createLspServerDescriptor(project: Project): JSFrameworkLspClientDescriptor =
    VueLspClientHybridModeDescriptor(project, runtime)

  override fun createWidgetItem(lspClient: LspClient, currentFile: VirtualFile?): LspClientWidgetItem =
    JSLspClientWidgetItem(
      lspClient = lspClient,
      currentFile = currentFile,
      itemIcon = VuejsIcons.Vue,
      statusBarIcon = VuejsIcons.Vue,
      settingsPageClass = VueConfigurable::class.java,
    )

  companion object {
    fun getProviderClass(runtime: VueServiceRuntime): Class<out JSFrameworkLspIntegrationProvider> {
      return when (runtime) {
        is VueServiceRuntime.Bundled -> when (runtime.version) {
          VueLanguageToolsVersion.DEFAULT ->
            VueLspIntegrationHybridModeDefaultProvider::class.java

          VueLanguageToolsVersion.LEGACY ->
            VueLspIntegrationHybridModeLegacyProvider::class.java
        }

        VueServiceRuntime.Manual ->
          VueLspIntegrationHybridModeManualProvider::class.java
      }
    }
  }
}
