// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.typescript.lsp.JSFrameworkLspServerDescriptor
import com.intellij.lang.typescript.lsp.JSFrameworkLspServerSupportProvider
import com.intellij.lang.typescript.lsp.JSLspServerWidgetItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem
import org.jetbrains.astro.AstroIcons
import org.jetbrains.astro.service.settings.AstroServiceConfigurable


class AstroLspServerSupportProvider : JSFrameworkLspServerSupportProvider(AstroServiceSetActivationRule) {
  override fun createLspServerDescriptor(project: Project): JSFrameworkLspServerDescriptor = AstroLspServerDescriptor(project)

  override fun createLspServerWidgetItem(lspServer: LspServer, currentFile: VirtualFile?): LspServerWidgetItem =
    JSLspServerWidgetItem(lspServer, currentFile, AstroIcons.Astro, AstroIcons.Astro, AstroServiceConfigurable::class.java)
}

class AstroLspServerDescriptor(project: Project) : JSFrameworkLspServerDescriptor(project, AstroServiceSetActivationRule, "Astro")
