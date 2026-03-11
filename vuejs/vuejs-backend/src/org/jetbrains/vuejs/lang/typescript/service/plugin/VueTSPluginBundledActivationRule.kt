// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.plugin

import com.intellij.lang.typescript.lsp.ServiceActivationHelper
import com.intellij.lang.typescript.lsp.TSPluginActivationRule
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.vuejs.lang.typescript.service.decideVueLSBundledVersion
import org.jetbrains.vuejs.lang.typescript.service.isVueServiceContext
import org.jetbrains.vuejs.options.VueLSMode
import org.jetbrains.vuejs.options.VueSettings

class VueTSPluginBundledActivationRule(
  version: VueTSPluginVersion,
) : TSPluginActivationRule(
  tsPluginLoader = VueTSPluginBundledLoaderFactory.getLoader(version),
  activationRule = VueTSPluginBundledActivationHelper(version),
)

private class VueTSPluginBundledActivationHelper(
  private val version: VueTSPluginVersion,
) : ServiceActivationHelper {
  override fun isProjectContext(project: Project, context: VirtualFile): Boolean {
    return isVueServiceContext(project, context)
           && decideVueLSBundledVersion(project, context) == version
  }

  override fun isEnabledInSettings(project: Project): Boolean {
    val settings = VueSettings.instance(project)
    return settings.serviceType == VueLSMode.AUTO
  }
}