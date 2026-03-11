// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.plugin

import com.intellij.lang.typescript.lsp.ServiceActivationHelper
import com.intellij.lang.typescript.lsp.TSPluginActivationRule
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.getAppropriateVueLSVersion
import org.jetbrains.vuejs.lang.typescript.service.isVueServiceContext
import org.jetbrains.vuejs.options.VueLSMode
import org.jetbrains.vuejs.options.VueSettings

class VueTSPluginActivationRule(
  runtime: VueServiceRuntime,
) : TSPluginActivationRule(
  tsPluginLoader = VueTSPluginLoaderFactory.getLoader(runtime),
  activationRule = ActivationHelper(runtime),
)

private class ActivationHelper(
  private val runtime: VueServiceRuntime,
) : ServiceActivationHelper {
  override fun isProjectContext(project: Project, context: VirtualFile): Boolean {
    val runtimeMatchesSettings = when (runtime) {
      is VueServiceRuntime.Manual -> true

      is VueServiceRuntime.Bundled ->
        getAppropriateVueLSVersion(project, context) == runtime.version
    }

    return isVueServiceContext(project, context) && runtimeMatchesSettings
  }

  override fun isEnabledInSettings(project: Project): Boolean {
    val settings = VueSettings.instance(project)
    return when (runtime) {
      is VueServiceRuntime.Bundled ->
        settings.serviceType == VueLSMode.AUTO

      is VueServiceRuntime.Manual ->
        settings.serviceType == VueLSMode.MANUAL
        && (settings.manualSettings.mode == VueSettings.ManualMode.ONLY_TS_PLUGIN
            || settings.manualSettings.mode == VueSettings.ManualMode.HYBRID_MODE)
    }
  }
}
