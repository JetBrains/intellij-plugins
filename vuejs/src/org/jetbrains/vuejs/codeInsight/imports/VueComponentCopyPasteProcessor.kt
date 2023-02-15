// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.imports

import com.intellij.html.webSymbols.elements.WebSymbolElementDescriptor
import com.intellij.javascript.web.js.WebJSResolveUtil.disableIndexUpToDateCheckIn
import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.editor.ES6CopyPasteProcessorBase
import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclarationPart
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.refactoring.ES6ReferenceExpressionsInfo
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlRecursiveElementWalkingVisitor
import com.intellij.psi.util.parentOfTypes
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.webSymbols.PsiSourcedWebSymbol
import com.intellij.xml.util.XmlTagUtil
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.editor.VueComponentSourceEdit
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.index.resolveLocally
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.html.parser.VueFile
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueContainer
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.source.COMPONENTS_PROP
import java.awt.datatransfer.DataFlavor
import kotlin.Pair
import com.intellij.openapi.util.Pair as OpenApiPair

class VueComponentCopyPasteProcessor : ES6CopyPasteProcessorBase<VueComponentCopyPasteProcessor.VueComponentImportsTransferableData>() {

  override val dataFlavor: DataFlavor
    get() = VUE_COMPONENT_IMPORTS_FLAVOR

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
    parent.acceptChildren(object : JSRecursiveWalkingElementVisitor() {
      override fun visitJSElement(node: JSElement) {
        if (node is JSEmbeddedContentImpl && textRange.intersects(node.textRange)) {
          result = true
          stopWalking()
        }
      }
    })
    return result && isAcceptablePasteContext(parent)
  }

  override fun alreadyHasImport(actualImportedName: String, importedElement: ImportedElement, scope: PsiElement): Boolean =
    if (scope.containingFile.asSafely<XmlFile>()?.let { findScriptTag(it, true) } != null)
      super.alreadyHasImport(actualImportedName, importedElement, scope)
    else
      disableIndexUpToDateCheckIn(scope) {
        val container = VueModelManager.findEnclosingContainer(scope) as? VueContainer
                        ?: return@disableIndexUpToDateCheckIn false
        var result = false
        val componentImportName = toAsset(actualImportedName)
        container.acceptEntities(object : VueModelVisitor() {
          override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
            result = result || toAsset(name) == componentImportName
            return !result
          }
        })
        result
      }

  override fun getExportScope(file: PsiFile, caret: Int): PsiElement? =
    super.getExportScope(file, caret)
    ?: WriteAction.compute<PsiElement, Throwable> {
      VueComponentSourceEdit.getOrCreateScriptScope(VueModelManager.findEnclosingContainer(file))
    }

  override fun processTextRanges(textRanges: List<Pair<PsiElement, TextRange>>): Set<ImportedElement> {
    val textRangesOnly = textRanges.map { it.second }
    val result = mutableSetOf<ImportedElement>()

    textRanges.forEach { (parent, range) ->
      val elements = mutableListOf<OpenApiPair<String, ES6ImportExportDeclarationPart>>()
      disableIndexUpToDateCheckIn(parent) {
        parent.accept(object : XmlRecursiveElementWalkingVisitor() {
          override fun visitXmlTag(tag: XmlTag) {
            super.visitXmlTag(tag)
            if (XmlTagUtil.getStartTagRange(tag)?.let { range.intersects(it) } == true) {
              val source = tag.descriptor.asSafely<WebSymbolElementDescriptor>()
                ?.symbol?.asSafely<PsiSourcedWebSymbol>()
                ?.source
                ?.let { source ->
                  if (source is JSProperty) {
                    source.value
                      ?.asSafely<JSReferenceExpression>()
                      ?.let { resolveLocally(it) }
                      ?.firstOrNull()
                  }
                  else source
                }
              if (source != null) {
                val capitalizedName = toAsset(tag.name, true)
                if (source is ES6ImportExportDeclarationPart) {
                  elements.add(OpenApiPair(capitalizedName, source))
                }
                else if (source is JSImplicitElement && source.context is VueFile) {
                  val file = source.context as VueFile
                  val path = getModuleNameOrPath(file)
                  if (path != null && capitalizedName.isNotBlank()) {
                    result.add(ImportedElement(
                      path, ES6ImportPsiUtil.CreateImportExportInfo(capitalizedName, ES6ImportPsiUtil.ImportExportType.DEFAULT), false))
                  }
                }
              }
            }
          }
        })
      }
      result.addAll(toImportedElements(listOf(ES6ReferenceExpressionsInfo.getInfoForImportDeclarations(elements)), textRangesOnly))
    }
    return result
  }

  override fun createTransferableData(importedElements: ArrayList<ImportedElement>): VueComponentImportsTransferableData =
    VueComponentImportsTransferableData(importedElements)

  override fun insertRequiredImports(pasteContext: PsiElement,
                                     data: VueComponentImportsTransferableData,
                                     destinationModule: PsiElement,
                                     imports: Collection<com.intellij.openapi.util.Pair<ES6ImportPsiUtil.CreateImportExportInfo, PsiElement>>,
                                     pasteContextLanguage: Language) {
    if (imports.isEmpty()) return
    WriteAction.run<RuntimeException> {
      val componentSourceEdit = VueComponentSourceEdit.create(VueModelManager.findEnclosingContainer(pasteContext))
                                ?: return@run
      val scriptScope = componentSourceEdit.getOrCreateScriptScope() ?: return@run
      for (import in imports) {
        val info = import.first
        val elementToImport = import.second
        val name = info.effectiveName
        if (componentSourceEdit.isScriptSetup() || componentSourceEdit.addClassicPropertyReference(COMPONENTS_PROP, name)) {
          ES6ImportPsiUtil.insertJSImport(scriptScope, info, elementToImport)
        }
      }
      componentSourceEdit.reformatChanges()
    }
  }

  class VueComponentImportsTransferableData(list: ArrayList<ImportedElement>) : ES6ImportsTransferableDataBase(list) {
    override fun getFlavor(): DataFlavor {
      return VUE_COMPONENT_IMPORTS_FLAVOR
    }
  }

  companion object {
    private val VUE_COMPONENT_IMPORTS_FLAVOR = DataFlavor(VueComponentImportsTransferableData::class.java, "vue component imports")
  }
}
