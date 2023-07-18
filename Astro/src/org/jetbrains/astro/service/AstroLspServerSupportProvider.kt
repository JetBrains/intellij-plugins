// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.javascript.library.typings.TypeScriptPackageName
import com.intellij.lang.typescript.lsp.JSFrameworkLspServerDescriptor
import com.intellij.lang.typescript.lsp.LspServerDownloader
import com.intellij.lang.typescript.lsp.getLspServerExecutablePath
import com.intellij.lang.typescript.lsp.scheduleLspServerDownloading
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import com.intellij.util.text.SemVer
import org.jetbrains.annotations.ApiStatus

internal val astroLanguageToolsVersion = SemVer.parseFromText("2.0.17")
internal const val npmPackage = "@astrojs/language-server"
private const val packageRelativePath = "/bin/nodeServer.js"
val serverPackageName = TypeScriptPackageName(npmPackage, astroLanguageToolsVersion)

class AstroLspServerSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    if (isServiceEnabledAndAvailable(project, file)) {
      serverStarter.ensureServerStarted(AstroLspServerDescriptor(project))
    }
  }
}

class AstroLspServerDescriptor(project: Project) : JSFrameworkLspServerDescriptor(project, "Astro") {
  override val relativeScriptPath = packageRelativePath
  override val npmPackage = serverPackageName

  override fun isSupportedFile(file: VirtualFile): Boolean = isFileAcceptableForService(file)
}

@ApiStatus.Experimental
object AstroLspExecutableDownloader : LspServerDownloader {
  override fun getExecutable(project: Project): String? = getLspServerExecutablePath(serverPackageName, packageRelativePath)

  override fun getExecutableOrRefresh(project: Project): String? {
    val executable = getExecutable(project)
    if (executable != null) return executable
    scheduleLspServerDownloading(project, serverPackageName)
    return null
  }
}