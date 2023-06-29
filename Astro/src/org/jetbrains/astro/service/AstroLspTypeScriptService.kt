// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.javascript.ecmascript6.TypeScriptAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.TypeScriptLanguageServiceAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptQuickInfoResponse
import com.intellij.lang.typescript.lsp.BaseLspTypeScriptService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.HtmlBuilder
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.util.convertMarkupContentToHtml
import com.intellij.psi.PsiFile
import org.eclipse.lsp4j.MarkupContent

class AstroLspTypeScriptService(project: Project) : BaseLspTypeScriptService(project, AstroLspServerSupportProvider::class.java) {
  override val name = "Astro LSP"
  override val prefix = "Astro"
  override val serverVersion = astroLanguageToolsVersion

  override fun createQuickInfoResponse(markupContent: MarkupContent): TypeScriptQuickInfoResponse {
    return TypeScriptQuickInfoResponse().apply {
      val content = HtmlBuilder().appendRaw(convertMarkupContentToHtml(markupContent)).toString()
      displayString = content.trim().let(StringUtil::unescapeXmlEntities)
    }
  }

  override fun canHighlight(file: PsiFile): Boolean {
    val provider = TypeScriptAnnotatorCheckerProvider.getCheckerProvider(file)
    if (provider !is TypeScriptLanguageServiceAnnotatorCheckerProvider) return false

    return isFileAcceptableForService(file.virtualFile ?: return false)
  }

  override fun isAcceptable(file: VirtualFile): Boolean = isServiceEnabledAndAvailable(project, file)
}