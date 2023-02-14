// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.imports

import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.editor.ES6CopyPasteProcessorBase
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.refactoring.ES6ReferenceExpressionsInfo
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.JSRecursiveWalkingElementVisitor
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlRecursiveElementWalkingVisitor
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import com.intellij.psi.util.parentOfTypes
import com.intellij.psi.util.parents
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.disableIndexUpToDateCheckIn
import org.jetbrains.vuejs.editor.VueComponentSourceEdit
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.html.parser.VueFile
import org.jetbrains.vuejs.model.VueContainer
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VueProperty
import java.awt.datatransfer.DataFlavor

class VueTemplateExprsCopyPasteProcessor : ES6CopyPasteProcessorBase<VueTemplateExprsCopyPasteProcessor.VueTemplateExprsImportsTransferableData>() {

  override val dataFlavor: DataFlavor
    get() = VUE_TEMPLATE_EXPRS_IMPORTS_FLAVOR

  override fun isAcceptableCopyContext(file: PsiFile, contextElements: List<PsiElement>): Boolean {
    val settings = JSApplicationSettings.getInstance()
    return file is VueFile && file.langMode.let {
      (it == LangMode.HAS_TS && settings.isUseTypeScriptAutoImport)
      || (it != LangMode.HAS_TS && settings.isUseJavaScriptAutoImport)
    }
  }

  override fun isAcceptablePasteContext(context: PsiElement): Boolean =
    context.containingFile is VueFile
    && context.parentOfTypes(JSExecutionScope::class, XmlTag::class, XmlDocument::class, withSelf = true)
      .let { it !is JSExecutionScope && it != null }

  override fun hasUnsupportedContentInCopyContext(parent: PsiElement, textRange: TextRange): Boolean {
    var result = false
    parent.accept(object : JSRecursiveWalkingElementVisitor() {
      override fun visitJSElement(node: JSElement) {
        if (node is JSEmbeddedContentImpl && textRange.intersects(node.textRange)) {
          result = true
          stopWalking()
        }
      }
    })
    return result || parent.parents(true).any { it is JSEmbeddedContentImpl }
  }

  override fun collectReferenceExpressions(parent: PsiElement, range: TextRange, addInfo: (ES6ReferenceExpressionsInfo) -> Unit): Boolean {
    if (!super.collectReferenceExpressions(parent, range, addInfo)) return false
    // We need to collect injected Vue JS expressions
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

  override fun alreadyHasImport(actualImportedName: String, importedElement: ImportedElement, scope: PsiElement): Boolean =
    if (scope.containingFile.asSafely<XmlFile>()?.let { findScriptTag(it, true) } != null)
      super.alreadyHasImport(actualImportedName, importedElement, scope)
    else
      disableIndexUpToDateCheckIn(scope) {
        val container = VueModelManager.findEnclosingContainer(scope) as? VueContainer
                        ?: return@disableIndexUpToDateCheckIn false
        var result = false
        container.acceptPropertiesAndMethods(object : VueModelVisitor() {
          override fun visitProperty(property: VueProperty, proximity: Proximity): Boolean {
            result = result || property.name == actualImportedName
            return !result
          }
        }, false)
        result
      }

  override fun createTransferableData(importedElements: ArrayList<ImportedElement>): VueTemplateExprsImportsTransferableData =
    VueTemplateExprsImportsTransferableData(importedElements)

  override fun getExportScope(file: PsiFile, caret: Int): PsiElement? {
    return super.getExportScope(file, caret)
           ?: WriteAction.compute<PsiElement, Throwable> {
             VueComponentSourceEdit.getOrCreateScriptScope(
               disableIndexUpToDateCheckIn(file) { VueModelManager.findEnclosingContainer(file) })
           }
  }

  override fun processTextRanges(textRanges: List<kotlin.Pair<PsiElement, TextRange>>): Set<ImportedElement> {
    return super.processTextRanges(textRanges)
  }

  override fun insertRequiredImports(data: VueTemplateExprsImportsTransferableData,
                                     destinationModule: PsiElement,
                                     imports: Collection<Pair<ES6ImportPsiUtil.CreateImportExportInfo, PsiElement>>,
                                     pasteContextLanguage: Language) {
    WriteAction.run<RuntimeException> {
      ES6CreateImportUtil.addRequiredImports(destinationModule, VueJSLanguage.INSTANCE, imports)
    }
  }

  class VueTemplateExprsImportsTransferableData(list: ArrayList<ImportedElement>) : ES6ImportsTransferableDataBase(list) {
    override fun getFlavor(): DataFlavor {
      return VUE_TEMPLATE_EXPRS_IMPORTS_FLAVOR
    }
  }

  companion object {
    private val VUE_TEMPLATE_EXPRS_IMPORTS_FLAVOR = DataFlavor(VueTemplateExprsImportsTransferableData::class.java, "vue es6 imports")
  }
}