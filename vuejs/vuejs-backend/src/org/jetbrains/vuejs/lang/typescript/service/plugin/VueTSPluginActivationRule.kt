// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.plugin

import com.intellij.lang.typescript.lsp.ServiceActivationHelper
import com.intellij.lang.typescript.lsp.TSPluginActivationRule
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.getAppropriateVueLSVersion
import org.jetbrains.vuejs.lang.typescript.service.isVueServiceCompatibleTypeScriptEnabled
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
    return isVueServiceContext(project, context)
           && getAppropriateVueLSVersion(project, context) == runtime.version
  }

  override fun isEnabledInSettings(project: Project): Boolean {
    if (!isVueServiceCompatibleTypeScriptEnabled(project))
      return false

    val settings = VueSettings.instance(project)
    return settings.serviceType == VueLSMode.AUTO
  }
}
