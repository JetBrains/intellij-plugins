// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.volar

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.typescript.lsp.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.lang.typescript.service.isVolarEnabledAndAvailable
import org.jetbrains.vuejs.lang.typescript.service.isVolarFileTypeAcceptable
import org.jetbrains.vuejs.options.getVueSettings

val volarLspServerPackageDescriptor = LspServerPackageDescriptor("@vue/language-server",
                                                                 "1.8.2",
                                                                 "/bin/vue-language-server.js")

class VolarSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    if (isVolarEnabledAndAvailable(project, file)) {
      serverStarter.ensureServerStarted(VolarServerDescriptor(project))
    }
  }
}

class VolarServerDescriptor(project: Project) : JSFrameworkLspServerDescriptor(project, volarLspServerPackageDescriptor, "Vue") {
  override fun isSupportedFile(file: VirtualFile): Boolean = isVolarFileTypeAcceptable(file)
}

@ApiStatus.Experimental
object VolarExecutableDownloader : LspServerDownloader(volarLspServerPackageDescriptor) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getVueSettings(project).packageRef
  }
}
