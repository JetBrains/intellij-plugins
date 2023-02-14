// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.imports

import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.editor.ES6CopyPasteProcessorBase
import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclarationPart
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil.ImportExportType
import com.intellij.lang.ecmascript6.refactoring.ES6ReferenceExpressionsInfo
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.modules.imports.JSImportAction
import com.intellij.lang.javascript.modules.imports.JSImportCandidateWithExecutor
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlRecursiveElementWalkingVisitor
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import com.intellij.psi.util.contextOfType
import com.intellij.psi.util.parentOfTypes
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.util.asSafely
import org.angular2.entities.Angular2ComponentLocator
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.Angular2HtmlFile
import java.awt.datatransfer.DataFlavor

class Angular2ExpressionsCopyPasteProcessor : ES6CopyPasteProcessorBase<Angular2ExpressionsCopyPasteProcessor.Angular2ExpressionsImportsTransferableData>() {

  override val dataFlavor: DataFlavor
    get() = ANGULAR2_EXPRESSIONS_IMPORTS_FLAVOR

  override fun isAcceptableCopyContext(file: PsiFile, contextElements: List<PsiElement>): Boolean {
    val settings = JSApplicationSettings.getInstance()
    return settings.isUseTypeScriptAutoImport
           && file is Angular2HtmlFile || contextElements.all { it.containingFile is Angular2HtmlFile }
  }

  override fun isAcceptablePasteContext(context: PsiElement): Boolean =
    context.containingFile is Angular2HtmlFile
    && context.parentOfTypes(JSExecutionScope::class, XmlTag::class, XmlDocument::class, withSelf = true)
      .let { it !is JSExecutionScope && it != null }

  override fun hasUnsupportedContentInCopyContext(parent: PsiElement, textRange: TextRange): Boolean =
    false

  override fun collectReferenceExpressions(parent: PsiElement, range: TextRange, addInfo: (ES6ReferenceExpressionsInfo) -> Unit): Boolean {
    if (!super.collectReferenceExpressions(parent, range, addInfo)) return false
    // We need to collect injected Angular 2 expressions as well
    val injectedLanguageManager = InjectedLanguageManager.getInstance(parent.project)
    parent.acceptChildren(object : XmlRecursiveElementWalkingVisitor() {
      override fun visitXmlText(text: XmlText) {
        injectedLanguageManager.enumerate(text) { injectedPsi, _ ->

          @Suppress("DEPRECATION")
          val injectionWindow = InjectedLanguageUtil.getDocumentWindow(injectedPsi)

          @Suppress("DEPRECATION")
          val adjustedRange = if (injectionWindow != null)
            TextRange(InjectedLanguageUtil.hostToInjectedUnescaped(injectionWindow, range.startOffset),
                      InjectedLanguageUtil.hostToInjectedUnescaped(injectionWindow, range.endOffset))
          else
            range

          addInfo(ES6ReferenceExpressionsInfo.getInfo(injectedPsi, adjustedRange))
        }
      }
    })
    return true
  }

  override fun createTransferableData(importedElements: ArrayList<ImportedElement>): Angular2ExpressionsImportsTransferableData =
    Angular2ExpressionsImportsTransferableData(importedElements)

  override fun getExportScope(file: PsiFile, caret: Int): PsiElement? =
    Angular2ComponentLocator.findComponentClass(getContextElementOrFile(file, caret))?.containingFile

  override fun insertRequiredImports(pasteContext: PsiElement,
                                     data: Angular2ExpressionsImportsTransferableData,
                                     destinationModule: PsiElement,
                                     imports: Collection<Pair<ES6ImportPsiUtil.CreateImportExportInfo, PsiElement>>,
                                     pasteContextLanguage: Language) {
    val globalImports = data.importedElements.mapNotNull {
      if (it.myInfo.importType == ImportExportType.BARE && it.myPath == "")
        Angular2GlobalImportCandidate(it.myInfo.importedName ?: return@mapNotNull null, pasteContext)
      else
        null
    }
    if (imports.isNotEmpty() || globalImports.isNotEmpty()) {
      WriteAction.run<RuntimeException> {
        ES6CreateImportUtil.addRequiredImports(pasteContext, Angular2Language.INSTANCE, imports)

        globalImports.forEach {
          JSImportAction(null, pasteContext, it.name)
            .executeFor(JSImportCandidateWithExecutor(it, Angular2AddImportExecutor(pasteContext)), null)
        }
      }
    }
  }

  override fun toImportedElements(expressions: List<ES6ReferenceExpressionsInfo>, ranges: Collection<TextRange>): Set<ImportedElement> {
    val result = super.toImportedElements(expressions, ranges).toMutableSet()
    val imports = mutableListOf<Pair<String, ES6ImportExportDeclarationPart>>()
    for (expressionsInfo in expressions) {
      for (localElement in expressionsInfo.localReferencedElements) {
        val referenceName = localElement
          .asSafely<TypeScriptField>()
          ?.initializerOrStub
          ?.asSafely<JSReferenceExpression>()
          ?.takeIf { it.qualifier == null }
          ?.referenceName
        val resolved = JSStubBasedPsiTreeUtil.resolveLocally(
          referenceName ?: continue,
          localElement.contextOfType<TypeScriptClass>()?.context ?: continue,
          false
        )
        if (resolved is ES6ImportExportDeclarationPart) {
          imports.add(Pair(referenceName, resolved))
        }
        else if (resolved == null) {
          // This is most likely a global import
          result.add(ImportedElement(
            "", ES6ImportPsiUtil.CreateImportExportInfo(referenceName, ImportExportType.BARE), true))
        }
      }
    }
    if (imports.isNotEmpty()) {
      result.addAll(super.toImportedElements(listOf(ES6ReferenceExpressionsInfo.getInfoForImportDeclarations(imports)), ranges))
    }
    return result
  }

  class Angular2ExpressionsImportsTransferableData(list: ArrayList<ImportedElement>) : ES6ImportsTransferableDataBase(list) {
    override fun getFlavor(): DataFlavor {
      return ANGULAR2_EXPRESSIONS_IMPORTS_FLAVOR
    }
  }

  companion object {
    private val ANGULAR2_EXPRESSIONS_IMPORTS_FLAVOR = DataFlavor(Angular2ExpressionsImportsTransferableData::class.java,
                                                                 "Angular2 es6 imports")
  }
}