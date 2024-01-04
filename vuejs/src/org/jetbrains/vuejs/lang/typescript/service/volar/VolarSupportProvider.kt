// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.volar

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.lsp.JSFrameworkLspServerDescriptor
import com.intellij.lang.typescript.lsp.LspServerDownloader
import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.OSAgnosticPathUtil.startsWithWindowsDrive
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.lang.typescript.service.isVolarEnabledAndAvailable
import org.jetbrains.vuejs.lang.typescript.service.isVolarFileTypeAcceptable
import org.jetbrains.vuejs.options.getVueSettings

private val volarLspServerPackageDescriptor: () -> LspServerPackageDescriptor = {
  LspServerPackageDescriptor("@vue/language-server",
                             Registry.stringValue("vue.language.server.default.version"),
                             "/bin/vue-language-server.js")
}

class VolarSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    if (isVolarEnabledAndAvailable(project, file)) {
      serverStarter.ensureServerStarted(VolarServerDescriptor(project))
    }
  }
}

class VolarServerDescriptor(project: Project) : JSFrameworkLspServerDescriptor(project, VolarExecutableDownloader, "Vue") {
  override fun isSupportedFile(file: VirtualFile): Boolean = isVolarFileTypeAcceptable(file)

  override fun getFileUri(file: VirtualFile): String {
    val uri = super.getFileUri(file)
    val prefix = "file:///"
    if (uri.startsWith(prefix) && startsWithWindowsDrive(uri.substring(prefix.length))) {
      // VS Code always sends lowercased Windows drive letters, and always escapes colon
      // See the issue and the related PR: https://github.com/microsoft/vscode-languageserver-node/issues/1280
      // The LSP spec requires that all servers work fine with both `file:///C:/foo` and `file:///c%3A/foo`,
      // but apparently some servers do not
      return prefix + uri[prefix.length].lowercase() + "%3A" + uri.substring(prefix.length + 2)
    }
    return uri
  }
}

@ApiStatus.Experimental
object VolarExecutableDownloader : LspServerDownloader(volarLspServerPackageDescriptor()) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getVueSettings(project).packageRef
  }
}
