package com.intellij.deno.service

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.deno.DenoSettings
import com.intellij.lang.ecmascript6.psi.ES6FromClause
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.javascript.JSTargetElementEvaluator
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceEvents
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.compiler.languageService.ide.TypeScriptLanguageServiceCompletionContributor
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptResponseCommon.FileSpan
import com.intellij.lang.typescript.psi.TypeScriptPsiUtil
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager

class DenoTypeScriptServiceGotoDeclarationHandler : GotoDeclarationHandler {
  override fun getGotoDeclarationTargets(sourceElement: PsiElement?,
                                         offset: Int,
                                         editor: Editor): Array<PsiElement>? {
    if (sourceElement == null) return PsiElement.EMPTY_ARRAY
    val project = sourceElement.project
    if (!DenoSettings.getService(project).isUseDeno()) return null

    val parent = sourceElement.parent
    if (parent is ES6FromClause) {
      if (parent.resolveReferencedElements().isNotEmpty()) return null
      val results = getResultsFromTypeScriptService(project, editor.document, sourceElement)
      if (!results.isNullOrEmpty()) return results
    }
    
    if (parent is ES6ImportSpecifier) {
      val resolved = parent.multiResolve(false)
      if (resolved.isNotEmpty()) return null
      val results = getResultsFromTypeScriptService(project, editor.document, sourceElement)
      if (!results.isNullOrEmpty()) return results
    }

    return null
  }

  private fun getResultsFromTypeScriptService(project: Project,
                                              document: Document?,
                                              sourceElement: PsiElement): Array<PsiElement>? {
    var document = document
    if (document == null) {
      document = PsiDocumentManager.getInstance(project).getDocument(sourceElement.containingFile)
      if (document == null) return null
    }
    val future = TypeScriptLanguageServiceEvents.getService(project).getDeclaration(sourceElement, document) ?: return null
    val spans = JSLanguageServiceUtil.awaitFuture(future,
                                                  TypeScriptLanguageServiceCompletionContributor.TIMEOUT_MILLS,
                                                  JSLanguageServiceUtil.QUOTA_MILLS, null) ?: return emptyArray()
    val psiManager = PsiManager.getInstance(project)
    val result = mutableListOf<PsiElement>()

    for (span in spans) {
      val path = span.file.path
      val file = LocalFileSystem.getInstance().findFileByPath(path) ?: continue
      val psiFile = psiManager.findFile(file) ?: continue
      val newDocument = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: continue
      val element = getElement(psiFile, newDocument, span) ?: continue
      val ajusted = JSTargetElementEvaluator.adjustTargetElement(element, null) ?: continue
      result.add(ajusted)
    }

    return result.toTypedArray()
  }

  private fun getElement(psiFile: PsiFile,
                         document: Document,
                         span: FileSpan): PsiElement? {
    val start = span.start
    val end = span.end
    val info = TypeScriptLanguageServiceUtil.getPsiElementInfo(psiFile, document, start, end) ?: return null
    run {
      val element = info.element
      if (element != null) {
        return element
      }
    }
    val range = info.range ?: return null
    return TypeScriptPsiUtil.getPsiElementByRange(psiFile, range)
  }

}