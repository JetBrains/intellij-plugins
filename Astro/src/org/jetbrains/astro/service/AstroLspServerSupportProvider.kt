// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.typescript.lsp.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import org.jetbrains.annotations.ApiStatus

val astroLspServerPackageDescriptor = LspServerPackageDescriptor("@astrojs/language-server",
                                                                 "2.0.17",
                                                                 "/bin/nodeServer.js")

class AstroLspServerSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    if (isServiceEnabledAndAvailable(project, file)) {
      serverStarter.ensureServerStarted(AstroLspServerDescriptor(project))
    }
  }
}

class AstroLspServerDescriptor(project: Project) : JSFrameworkLspServerDescriptor(project, astroLspServerPackageDescriptor, "Astro") {
  override fun isSupportedFile(file: VirtualFile): Boolean = isFileAcceptableForService(file)
}

@ApiStatus.Experimental
object AstroLspExecutableDownloader : LspServerDownloader(astroLspServerPackageDescriptor)