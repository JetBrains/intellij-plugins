// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.volar

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.ecmascript6.TypeScriptAnnotatorCheckerProvider
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.lang.typescript.compiler.TypeScriptLanguageServiceAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptQuickInfoResponse
import com.intellij.lang.typescript.lsp.JSFrameworkLspTypeScriptService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerDescriptor
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.psi.PsiFile
import com.intellij.util.text.SemVer
import org.jetbrains.vuejs.lang.typescript.service.isVolarEnabledAndAvailable
import org.jetbrains.vuejs.lang.typescript.service.isVolarFileTypeAcceptable
import org.jetbrains.vuejs.options.VueServiceSettings
import org.jetbrains.vuejs.options.getVueSettings

class VolarTypeScriptService(project: Project) : JSFrameworkLspTypeScriptService(project) {
  override fun getProviderClass(): Class<out LspServerSupportProvider> = VolarSupportProvider::class.java

  override val name = "Volar"
  override val serverVersion: SemVer
    get() = calculateVersion()

  private fun calculateVersion(): SemVer {
    return VolarExecutableDownloader.getExecutable(project)
             ?.let { LocalFileSystem.getInstance().findFileByPath(it) }
             ?.let { PackageJsonUtil.findUpPackageJson(it) }
             ?.let { PackageJsonData.getOrCreate(it).version } ?: defaultVolarVersion
  }

  override fun createQuickInfoResponse(rawResponse: String): TypeScriptQuickInfoResponse {
    return TypeScriptQuickInfoResponse().apply {
      displayString = rawResponse.removeSurrounding("<html><body><pre>", "</pre></body></html>")
    }
  }

  override fun canHighlight(file: PsiFile): Boolean {
    val provider = TypeScriptAnnotatorCheckerProvider.getCheckerProvider(file)
    if (provider !is TypeScriptLanguageServiceAnnotatorCheckerProvider) return false

    return isVolarFileTypeAcceptable(file.virtualFile ?: return false)
  }

  override fun isAcceptable(file: VirtualFile) = isVolarEnabledAndAvailable(project, file)

  override fun isServiceEnabledBySettings(project: Project): Boolean {
    return getVueSettings(project).serviceType == VueServiceSettings.VOLAR
  }

  override fun getLspServerDescriptor(project: Project, file: VirtualFile): LspServerDescriptor? {
    return getVolarServerDescriptor(project, file)
  }
}