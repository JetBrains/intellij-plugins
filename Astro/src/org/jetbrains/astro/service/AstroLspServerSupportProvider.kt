// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.typescript.lsp.JSFrameworkLspServerDescriptor
import com.intellij.lang.typescript.lsp.LspServerDownloader
import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.astro.AstroIcons
import org.jetbrains.astro.service.settings.AstroServiceConfigurable

private val astroLspServerPackageDescriptor: () -> LspServerPackageDescriptor = {
  LspServerPackageDescriptor("@astrojs/language-server",
                             Registry.stringValue("astro.language.server.default.version"),
                             "/bin/nodeServer.js")
}

class AstroLspServerSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    if (isServiceEnabledAndAvailable(project, file)) {
      serverStarter.ensureServerStarted(AstroLspServerDescriptor(project))
    }
  }

  override fun createLspServerWidgetItem(lspServer: LspServer, currentFile: VirtualFile?): LspServerWidgetItem =
    LspServerWidgetItem(lspServer, currentFile, AstroIcons.Astro, AstroServiceConfigurable::class.java)
}

class AstroLspServerDescriptor(project: Project) : JSFrameworkLspServerDescriptor(project, AstroLspExecutableDownloader, "Astro") {
  override fun isSupportedFile(file: VirtualFile): Boolean = isFileAcceptableForService(file)
}

@ApiStatus.Experimental
object AstroLspExecutableDownloader : LspServerDownloader(astroLspServerPackageDescriptor())