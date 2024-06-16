// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.volar

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.typescript.lsp.*
import com.intellij.lang.typescript.resolve.TypeScriptCompilerEvaluationFacade
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.lang.typescript.service.isLspServerEnabledAndAvailable
import org.jetbrains.vuejs.lang.typescript.service.isFileAcceptableForLspServer
import org.jetbrains.vuejs.options.VueConfigurable
import org.jetbrains.vuejs.options.getVueSettings
import java.io.File

private val volarLspServerPackageDescriptor: () -> LspServerPackageDescriptor = {
  LspServerPackageDescriptor("@vue/language-server",
                             Registry.stringValue("vue.language.server.default.version"),
                             "/bin/vue-language-server.js")
}

class VolarSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    if (isLspServerEnabledAndAvailable(project, file)) {
      serverStarter.ensureServerStarted(VolarServerDescriptor(project))
    }
  }

  override fun createLspServerWidgetItem(lspServer: LspServer, currentFile: VirtualFile?): LspServerWidgetItem =
    LspServerWidgetItem(lspServer, currentFile, VuejsIcons.Vue, VueConfigurable::class.java)
}

class VolarServerDescriptor(project: Project) : JSFrameworkLspServerDescriptor(project, VolarExecutableDownloader, "Vue") {
  override fun isSupportedFile(file: VirtualFile): Boolean = isFileAcceptableForLspServer(file)
}

@ApiStatus.Experimental
object VolarExecutableDownloader : LspServerDownloader(volarLspServerPackageDescriptor()) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getVueSettings(project).packageRef
  }

  override fun getExecutable(project: Project, packageRef: NodePackageRef): String? {
    val ref = extractRefText(packageRef)
    if (ref == defaultPackageKey) {
      if (TypeScriptCompilerEvaluationFacade.getInstance(project) != null) {
        // work in progress
        val file = File(TypeScriptUtil.getTypeScriptCompilerFolderFile(),
                        "typescript/node_modules/tsc-vue/${packageDescriptor.packageRelativePath}")
        val path = file.absolutePath
        return path
      }
      else {
        return getLspServerExecutablePath(packageDescriptor.serverPackage, packageDescriptor.packageRelativePath)
      }
    }

    val suffix = FileUtil.toSystemDependentName(packageDescriptor.packageRelativePath)

    return if (ref.endsWith(suffix)) ref else "$ref$suffix"
  }
}
