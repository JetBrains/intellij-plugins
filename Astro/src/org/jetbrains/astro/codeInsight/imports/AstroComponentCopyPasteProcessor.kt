// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight.imports

import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.editor.ES6CopyPasteProcessorBase
import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclarationPart
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.refactoring.ES6ReferenceExpressionsInfo
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlRecursiveElementWalkingVisitor
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.XmlTagUtil
import org.jetbrains.astro.codeInsight.frontmatterScript
import org.jetbrains.astro.editor.AstroComponentSourceEdit
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.lang.psi.AstroFrontmatterScript
import java.awt.datatransfer.DataFlavor
import kotlin.Pair
import com.intellij.openapi.util.Pair as OpenApiPair

class AstroComponentCopyPasteProcessor : ES6CopyPasteProcessorBase<AstroComponentCopyPasteProcessor.AstroComponentImportsTransferableData>() {

  override val dataFlavor: DataFlavor
    get() = ASTRO_COMPONENT_IMPORTS_FLAVOR

  override fun isAcceptableCopyContext(file: PsiFile, contextElements: List<PsiElement>): Boolean {
    val settings = JSApplicationSettings.getInstance()
    return file is AstroFileImpl && settings.isUseTypeScriptAutoImport
  }

  override fun isAcceptablePasteContext(context: PsiElement): Boolean =
    context.containingFile is AstroFileImpl
    && context.parentOfType<AstroFrontmatterScript>(withSelf = true) == null

  override fun hasUnsupportedContentInCopyContext(parent: PsiElement, textRange: TextRange): Boolean =
    parent.frontmatterScript()
      ?.let { textRange.intersects(it.textRange) } != false

  override fun alreadyHasImport(actualImportedName: String, importedElement: ImportedElement, scope: PsiElement): Boolean =
    scope.frontmatterScript()
      ?.let { super.alreadyHasImport(actualImportedName, importedElement, it) } == true

  override fun processTextRanges(textRanges: List<Pair<PsiElement, TextRange>>): Set<ImportedElement> {
    val textRangesOnly = textRanges.map { it.second }
    val result = mutableSetOf<ImportedElement>()

    textRanges.forEach { (parent, range) ->
      val elements = mutableListOf<OpenApiPair<String, ES6ImportExportDeclarationPart>>()
      parent.accept(object : XmlRecursiveElementWalkingVisitor() {
        override fun visitXmlTag(tag: XmlTag) {
          super.visitXmlTag(tag)
          if (XmlTagUtil.getStartTagRange(tag)?.let { range.intersects(it) } == true) {
            val name = tag.name
            if (name.firstOrNull()?.isUpperCase() == true) {
              val source = JSStubBasedPsiTreeUtil.resolveLocally(name, tag, false)
              if (source is ES6ImportExportDeclarationPart) {
                elements.add(OpenApiPair(name, source))
              }
            }
          }
        }
      })
      result.addAll(toImportedElements(listOf(ES6ReferenceExpressionsInfo.getInfoForImportDeclarations(elements)), textRangesOnly))
    }
    return result
  }

  override fun getExportScope(file: PsiFile, caret: Int): PsiElement? =
    super.getExportScope(file, caret)
    ?: WriteAction.compute<PsiElement, Throwable> {
      AstroComponentSourceEdit.getOrCreateFrontmatterScript(file)
    }

  override fun createTransferableData(importedElements: ArrayList<ImportedElement>): AstroComponentImportsTransferableData =
    AstroComponentImportsTransferableData(importedElements)

  override fun insertRequiredImports(pasteContext: PsiElement,
                                     data: AstroComponentImportsTransferableData,
                                     destinationModule: PsiElement,
                                     imports: Collection<OpenApiPair<ES6ImportPsiUtil.CreateImportExportInfo, PsiElement>>,
                                     pasteContextLanguage: Language) {
    WriteAction.run<RuntimeException> {
      ES6CreateImportUtil.addRequiredImports(destinationModule, pasteContextLanguage, imports)
    }
  }

  class AstroComponentImportsTransferableData(list: ArrayList<ImportedElement>) : ES6ImportsTransferableDataBase(list) {
    override fun getFlavor(): DataFlavor {
      return ASTRO_COMPONENT_IMPORTS_FLAVOR
    }
  }

  companion object {
    private val ASTRO_COMPONENT_IMPORTS_FLAVOR = DataFlavor(AstroComponentImportsTransferableData::class.java, "vue component imports")
  }
}
