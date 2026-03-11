// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.typescript.lsp.LspServerLoader
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.options.VueSettings

@ApiStatus.Experimental
internal object VueLspServerTakeoverModeLoader : LspServerLoader(
  VueLspServerPackageDescriptor("2.2.10")
) {
  override fun getSelectedPackage(project: Project): NodePackage {
    val settings = VueSettings.instance(project)
    return settings.manualSettings.lspServerPackage
  }
}