// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.volar

import com.google.gson.JsonElement
import com.intellij.lang.javascript.ecmascript6.TypeScriptAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.TypeScriptLanguageServiceAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.LspGetElementTypeRequestArgs
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptGetElementTypeRequestArgs
import com.intellij.lang.typescript.lsp.BaseLspTypeScriptService
import com.intellij.lang.typescript.lsp.JSFrameworkLsp4jServer
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.typescript.service.isVolarEnabledAndAvailable

class VolarTypeScriptService(project: Project) : BaseLspTypeScriptService(project, VolarSupportProvider::class.java) {
  override val name: String
    get() = VueBundle.message("vue.service.name")
  override val prefix: String
    get() = VueBundle.message("vue.service.prefix")

  override fun isAcceptable(file: VirtualFile) = isVolarEnabledAndAvailable(project, file)

  override fun getServiceId(): String = "vue"

  override suspend fun getIdeType(virtualFile: VirtualFile, args: TypeScriptGetElementTypeRequestArgs): JsonElement? {
    return withServer {
      val params = LspGetElementTypeRequestArgs(file = descriptor.getFileUri(virtualFile),
                                                range = args.range,
                                                forceReturnType = args.forceReturnType)
      sendRequest { (it as JSFrameworkLsp4jServer).getElementType(params) }
    }
  }
}