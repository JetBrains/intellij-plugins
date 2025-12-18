// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.lang.typescript.lsp.ServiceActivationHelper
import com.intellij.lang.typescript.lsp.TSPluginActivationRule
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.typescript.service.plugin.VueTSPluginLoader
import org.jetbrains.vuejs.options.getVueSettings

internal const val vuePluginPath = "vuejs/vuejs-backend"

internal fun isVueServiceContext(project: Project, context: VirtualFile): Boolean {
  return context.fileType is VueFileType || isVueContext(context, project)
}

class VueTSPluginActivationRule(loader: VueTSPluginLoader) :
  TSPluginActivationRule(loader, VueTSPluginActivationHelper) {
  override fun isEnabled(project: Project, context: VirtualFile): Boolean {
    if (!getVueSettings(project).tsPluginPreviewEnabled)
      return false

    return super.isEnabled(project, context)
  }
}

private object VueTSPluginActivationHelper : ServiceActivationHelper {
  override fun isProjectContext(project: Project, context: VirtualFile): Boolean {
    return isVueServiceContext(project, context)
  }

  override fun isEnabledInSettings(project: Project): Boolean {
    return getVueSettings(project).tsPluginPreviewEnabled
  }
}
