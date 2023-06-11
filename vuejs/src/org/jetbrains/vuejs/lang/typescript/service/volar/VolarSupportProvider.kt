// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.volar

import com.intellij.lang.javascript.library.typings.TypeScriptPackageName
import com.intellij.lang.typescript.lsp.JSFrameworkLspServerDescriptor
import com.intellij.lang.typescript.lsp.getLspServerExecutablePath
import com.intellij.lang.typescript.lsp.scheduleLspServerDownloading
import com.intellij.platform.lsp.api.LspServerDescriptor
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.SemVer
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.lang.typescript.service.isVolarEnabledAndAvailable

internal val volarVersion = SemVer.parseFromText("1.6.5")
private const val volarPackage = "@volar/vue-language-server"
private const val packageRelativePath = "/bin/vue-language-server.js"
val serverPackageName = TypeScriptPackageName(volarPackage, volarVersion)

class VolarSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    getVolarServerDescriptor(project, file)?.let { serverStarter.ensureServerStarted(it) }
  }
}

fun getVolarServerDescriptor(project: Project, file: VirtualFile): LspServerDescriptor? {
  if (!isVolarEnabledAndAvailable(project, file)) return null
  val projectDir = project.guessProjectDir() ?: return null
  return VolarServerDescriptor(project, projectDir)
}

class VolarServerDescriptor(project: Project, vararg roots: VirtualFile)
  : JSFrameworkLspServerDescriptor(project, "Vue", *roots) {
  override val relativeScriptPath = packageRelativePath
  override val npmPackage = serverPackageName

  override fun isSupportedFile(file: VirtualFile): Boolean {
    return isVolarEnabledAndAvailable(project, file) // circular JSFrameworkLspServerDescriptor.isSupportedFile && getVolarServerDescriptor
  }
}

@ApiStatus.Experimental
object VolarExecutableDownloader {
  fun getExecutable(): String? {
    return getLspServerExecutablePath(serverPackageName, packageRelativePath)
  }

  fun getExecutableOrRefresh(project: Project): String? {
    val executable = getExecutable()
    if (executable != null) return executable
    scheduleLspServerDownloading(project, serverPackageName)
    return null
  }
}
