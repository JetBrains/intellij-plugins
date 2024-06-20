// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.volar

import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.typescript.lsp.JSFrameworkLspServerDescriptor
import com.intellij.lang.typescript.lsp.JSLspServerWidgetItem
import com.intellij.lang.typescript.lsp.LspServerDownloader
import com.intellij.lang.typescript.lsp.LspServerPackageDescriptor
import com.intellij.lang.typescript.resolve.TypeScriptCompilerEvaluationFacade
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.LspServerSupportProvider.LspServerStarter
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.lang.typescript.service.VueServiceSetActivationRule
import org.jetbrains.vuejs.options.VueConfigurable
import org.jetbrains.vuejs.options.getVueSettings
import java.io.File

private object VolarLspServerPackageDescriptor : LspServerPackageDescriptor("@vue/language-server",
                                                                            "1.8.27",
                                                                            "/bin/vue-language-server.js") {
  override val defaultVersion: String get() = Registry.stringValue("vue.language.server.default.version")
}

class VolarSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    if (VueServiceSetActivationRule.isLspServerEnabledAndAvailable(project, file)) {
      serverStarter.ensureServerStarted(VolarServerDescriptor(project))
    }
  }

  override fun createLspServerWidgetItem(lspServer: LspServer, currentFile: VirtualFile?): LspServerWidgetItem =
    JSLspServerWidgetItem(lspServer, currentFile, VuejsIcons.Vue, VuejsIcons.Vue, VueConfigurable::class.java)
}

class VolarServerDescriptor(project: Project) : JSFrameworkLspServerDescriptor(project, VueServiceSetActivationRule, "Vue")

@ApiStatus.Experimental
object VolarExecutableDownloader : LspServerDownloader(VolarLspServerPackageDescriptor) {
  override fun getSelectedPackageRef(project: Project): NodePackageRef {
    return getVueSettings(project).packageRef
  }

  override fun getExecutableForDefaultKey(project: Project): String? {
    if (TypeScriptCompilerEvaluationFacade.getInstance(project) != null) {
      // work in progress
      val file = File(TypeScriptUtil.getTypeScriptCompilerFolderFile(),
                      "typescript/node_modules/tsc-vue/${packageDescriptor.packageRelativePath}")
      val path = file.absolutePath
      return path
    }

    return super.getExecutableForDefaultKey(project)
  }
}
