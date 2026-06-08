// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.intellij.lang.typescript.lsp.JSFrameworkLspClientDescriptor
import com.intellij.lang.typescript.lsp.JSFrameworkLspClientProvider
import com.intellij.lang.typescript.lsp.JSLspClientWidgetItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.lsWidget.LspClientWidgetItem
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.options.VueConfigurable

internal class VueLspClientHybridModeDefaultProvider :
  VueLspClientHybridModeProvider(VueServiceRuntime.Bundled(VueLanguageToolsVersion.DEFAULT))

internal class VueLspClientHybridModeLegacyProvider :
  VueLspClientHybridModeProvider(VueServiceRuntime.Bundled(VueLanguageToolsVersion.LEGACY))

internal class VueLspClientHybridModeManualProvider :
  VueLspClientHybridModeProvider(VueServiceRuntime.Manual)

sealed class VueLspClientHybridModeProvider(
  private val runtime: VueServiceRuntime,
) : JSFrameworkLspClientProvider(VueLspServerHybridModeActivationRule(runtime)) {

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
    fun getProviderClass(runtime: VueServiceRuntime): Class<out JSFrameworkLspClientProvider> {
      return when (runtime) {
        is VueServiceRuntime.Bundled -> when (runtime.version) {
          VueLanguageToolsVersion.DEFAULT ->
            VueLspClientHybridModeDefaultProvider::class.java

          VueLanguageToolsVersion.LEGACY ->
            VueLspClientHybridModeLegacyProvider::class.java
        }

        VueServiceRuntime.Manual ->
          VueLspClientHybridModeManualProvider::class.java
      }
    }
  }
}
