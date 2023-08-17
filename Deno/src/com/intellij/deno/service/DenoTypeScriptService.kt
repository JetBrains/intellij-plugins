package com.intellij.deno.service

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.deno.DenoSettings
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.frameworks.modules.JSUrlImportsUtil
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.library.JSCorePredefinedLibrariesProvider
import com.intellij.lang.javascript.service.JSLanguageServiceProvider
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.TypeScriptService.CompletionMergeStrategy
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptQuickInfoResponse
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider
import com.intellij.lang.typescript.lsp.BaseLspTypeScriptService
import com.intellij.lang.typescript.lsp.LspAnnotationError
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.HtmlBuilder
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.util.convertMarkupContentToHtml
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.text.SemVer
import org.eclipse.lsp4j.MarkupContent

class DenoTypeScriptServiceProvider(val project: Project) : JSLanguageServiceProvider {

  override fun isHighlightingCandidate(file: VirtualFile) = TypeScriptCompilerSettings.acceptFileType(file.fileType)

  override fun getService(file: VirtualFile) = allServices.firstOrNull()

  override fun getAllServices() =
    if (DenoSettings.getService(project).isUseDeno()) listOf(DenoTypeScriptService.getInstance(project)) else emptyList()
}

@Service(Service.Level.PROJECT)
class DenoTypeScriptService(project: Project) : BaseLspTypeScriptService(project, DenoLspSupportProvider::class.java) {
  companion object {
    fun getInstance(project: Project): DenoTypeScriptService = project.getService(DenoTypeScriptService::class.java)
  }

  override val name: String
    get() = "Deno LSP"
  override val prefix: String
    get() = "Deno"

  override fun getStatusText() = withServer {
    // TODO use super method (& display serverVersion)
    when {
      isRunning -> "Deno LSP"
      isMalfunctioned -> "Deno LSP ⚠"
      else -> "..."
    }
  }

  override fun getCompletionMergeStrategy(parameters: CompletionParameters, file: PsiFile, context: PsiElement): CompletionMergeStrategy {
    if (JSTokenTypes.STRING_LITERALS.contains(context.node.elementType)) {
      JSFileReferencesUtil.getReferenceModuleText(context.parent)?.let {
        if (JSUrlImportsUtil.startsWithRemoteUrlPrefix(JSStringUtil.unquoteStringLiteralValue(it))) {
          return CompletionMergeStrategy.MERGE
        }
      }
    }

    return TypeScriptLanguageServiceUtil.getCompletionMergeStrategy(parameters, file, context)
  }

  override fun getServiceFixes(file: PsiFile, element: PsiElement?, result: JSAnnotationError): Collection<IntentionAction> {
    return (result as? LspAnnotationError)?.quickFixes ?: emptyList()
  }

  override fun createQuickInfoResponse(markupContent: MarkupContent): TypeScriptQuickInfoResponse {
    return TypeScriptQuickInfoResponse().apply {
      val html = HtmlBuilder().appendRaw(convertMarkupContentToHtml(markupContent)).toString()
      displayString = html.removeSurrounding("<pre>", "</pre>")
    }
  }

  override fun canHighlight(file: PsiFile) = DialectDetector.isTypeScript(file)

  override fun isAcceptable(file: VirtualFile) =
    TypeScriptLanguageServiceUtil.ACCEPTABLE_TS_FILE.value(file) &&
    !JSCorePredefinedLibrariesProvider.isCoreLibraryFile(file) &&
    !DenoTypings.getInstance(project).isDenoTypings(file) &&
    !TypeScriptLibraryProvider.isLibraryOrBundledLibraryFile(project, file)

  override fun getServiceId(): String = "deno"
}