package com.intellij.deno.service

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.deno.DenoSettings
import com.intellij.deno.isDenoEnableForContextDirectory
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.library.JSCorePredefinedLibrariesProvider
import com.intellij.lang.javascript.service.JSLanguageServiceProvider
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider
import com.intellij.lang.typescript.lsp.BaseLspTypeScriptService
import com.intellij.lang.typescript.lsp.LspAnnotationError
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class DenoTypeScriptServiceProvider(val project: Project) : JSLanguageServiceProvider {

  override fun isHighlightingCandidate(file: VirtualFile) = TypeScriptLanguageServiceUtil.isTypeScriptFileType(file.fileType)

  override fun getService(file: VirtualFile) = allServices.firstOrNull()

  override val allServices
    get() = if (DenoSettings.getService(project).isUseDeno()) listOf(DenoTypeScriptService.getInstance(project)) else emptyList()
}

@Service(Service.Level.PROJECT)
class DenoTypeScriptService(project: Project) : BaseLspTypeScriptService(project, DenoLspSupportProvider::class.java) {
  companion object {
    fun getInstance(project: Project): DenoTypeScriptService = project.getService(DenoTypeScriptService::class.java)
  }

  override fun isServiceNavigationEnabled(): Boolean = true

  override val name: String
    get() = "Deno LSP"
  override val prefix: String
    get() = "Deno"

  override fun getServiceFixes(file: PsiFile, element: PsiElement?, result: JSAnnotationError): Collection<IntentionAction> {
    return (result as? LspAnnotationError)?.quickFixes ?: emptyList()
  }

  override fun isAcceptable(file: VirtualFile) =
    TypeScriptLanguageServiceUtil.ACCEPTABLE_TS_FILE.value(file) &&
    !JSCorePredefinedLibrariesProvider.isCoreLibraryFile(file) &&
    !DenoTypings.getInstance(project).isDenoTypings(file) &&
    TypeScriptLanguageServiceUtil.isTypeScriptFileType(file.fileType) &&
    !TypeScriptLibraryProvider.isLibraryOrBundledLibraryFile(project, file) &&
    isDenoEnableForContextDirectory(project, file)
}