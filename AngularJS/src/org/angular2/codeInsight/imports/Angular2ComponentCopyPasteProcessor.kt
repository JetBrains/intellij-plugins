// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.imports

import com.intellij.javascript.web.js.WebJSResolveUtil.disableIndexUpToDateCheckIn
import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.editor.ES6CopyPasteProcessorBase
import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclarationPart
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.refactoring.ES6ReferenceExpressionsInfo
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlRecursiveElementWalkingVisitor
import com.intellij.psi.util.parentOfTypes
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.xml.util.XmlTagUtil
import org.angular2.Angular2DecoratorUtil
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.tags.Angular2ElementDescriptor
import org.angular2.entities.Angular2ComponentLocator
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2Entity
import org.angular2.inspections.actions.NgModuleImportAction
import org.angular2.inspections.quickfixes.Angular2FixesPsiUtil
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression
import org.angular2.lang.html.Angular2HtmlFile
import java.awt.datatransfer.DataFlavor
import kotlin.Pair
import com.intellij.openapi.util.Pair as OpenApiPair

class Angular2ComponentCopyPasteProcessor : ES6CopyPasteProcessorBase<Angular2ComponentCopyPasteProcessor.Angular2ComponentImportsTransferableData>() {

  override val dataFlavor: DataFlavor
    get() = ANGULAR2_COMPONENT_IMPORTS_FLAVOR

  override fun isAcceptableCopyContext(file: PsiFile, contextElements: List<PsiElement>): Boolean {
    val settings = JSApplicationSettings.getInstance()
    return settings.isUseTypeScriptAutoImport
           && file is Angular2HtmlFile || contextElements.all { it.containingFile is Angular2HtmlFile }
  }

  override fun isAcceptablePasteContext(context: PsiElement): Boolean =
    context.containingFile is Angular2HtmlFile
    && context.parentOfTypes(JSExecutionScope::class, XmlTag::class, XmlDocument::class, withSelf = true)
      .let { it !is JSExecutionScope && it != null }

  override fun hasUnsupportedContentInCopyContext(parent: PsiElement, textRange: TextRange): Boolean {
    return !isAcceptablePasteContext(parent)
  }

  override fun alreadyHasImport(actualImportedName: String, importedElement: ImportedElement, scope: PsiElement): Boolean =
    false

  override fun getExportScope(file: PsiFile, caret: Int): PsiElement? =
    Angular2ComponentLocator.findComponentClass(getContextElementOrFile(file, caret))?.containingFile

  override fun processTextRanges(textRanges: List<Pair<PsiElement, TextRange>>): Set<ImportedElement> {
    val textRangesOnly = textRanges.map { it.second }
    val result = mutableSetOf<ImportedElement>()

    textRanges.forEach { (parent, range) ->
      val elements = mutableListOf<OpenApiPair<String, ES6ImportExportDeclarationPart>>()
      val classesToImport = disableIndexUpToDateCheckIn(parent) {
        val entities = mutableSetOf<Angular2Entity>()
        val scope = Angular2DeclarationsScope(parent)
        parent.accept(object : XmlRecursiveElementWalkingVisitor() {
          override fun visitXmlTag(tag: XmlTag) {
            super.visitXmlTag(tag)
            if (XmlTagUtil.getStartTagRange(tag)?.let { range.intersects(it) } == true) {
              tag.descriptor.asSafely<Angular2ElementDescriptor>()
                ?.sourceDirectives
                ?.let { entities.addAll(it) }
            }
          }

          override fun visitXmlAttribute(attribute: XmlAttribute) {
            super.visitXmlAttribute(attribute)
            if (attribute.nameElement?.textRange?.let { range.intersects(it) } == true) {
              attribute.descriptor.asSafely<Angular2AttributeDescriptor>()
                ?.sourceDirectives
                ?.let { entities.addAll(it) }
            }
          }

          override fun visitElement(element: PsiElement) {
            super.visitElement(element)
            if (element is Angular2PipeReferenceExpression) {
              Angular2EntitiesProvider.findPipes(element.project, element.referenceName!!)
                .find { scope.contains(it) }
                ?.let { entities.add(it) }
            }
          }
        })
        entities.asSequence()
          .mapNotNull { it.typeScriptClass }
          .distinct()
          .toList()
      }
      for (tsClass in classesToImport) {
        val name = tsClass.name
        if (name != null) {
          val path = getModuleNameOrPath(tsClass.containingFile)
          if (path != null) {
            result.add(ImportedElement(
              path, ES6ImportPsiUtil.CreateImportExportInfo(name, ES6ImportPsiUtil.ImportExportType.DEFAULT), false))
          }
        }
      }
      result.addAll(toImportedElements(listOf(ES6ReferenceExpressionsInfo.getInfoForImportDeclarations(elements)), textRangesOnly))
    }
    return result
  }

  override fun createTransferableData(importedElements: ArrayList<ImportedElement>): Angular2ComponentImportsTransferableData =
    Angular2ComponentImportsTransferableData(importedElements)

  override fun insertRequiredImports(pasteContext: PsiElement,
                                     data: Angular2ComponentImportsTransferableData,
                                     destinationModule: PsiElement,
                                     imports: Collection<OpenApiPair<ES6ImportPsiUtil.CreateImportExportInfo, PsiElement>>,
                                     pasteContextLanguage: Language) {
    if (imports.isEmpty()) return
    disableIndexUpToDateCheckIn(pasteContext) {
      val scope = Angular2DeclarationsScope(pasteContext)
      val importsOwner = scope.importsOwner
      val destinationModuleClass = importsOwner?.typeScriptClass
                                   ?: return@disableIndexUpToDateCheckIn
      val declarationsInScope = importsOwner.declarationsInScope
      val modulesToImport = imports
        .asSequence()
        .mapNotNull { pair ->
          resolveElement(destinationModuleClass, pair)
            ?.let { Angular2EntitiesProvider.getDeclaration(it) }
            ?.takeIf { it !in declarationsInScope }
        }
        .mapNotNull {
          NgModuleImportAction.declarationsToModuleImports(pasteContext, listOf(it), scope).firstOrNull()
        }
        .distinct()
        .sortedBy { it.name }
        .toList()

      if (modulesToImport.isNotEmpty()) {
        WriteAction.run<RuntimeException> {
          for (module in modulesToImport) {
            val element = module.element ?: continue
            ES6ImportPsiUtil.insertJSImport(destinationModuleClass, module.name, element, null)
            Angular2FixesPsiUtil.insertEntityDecoratorMember(importsOwner, Angular2DecoratorUtil.IMPORTS_PROP, module.name)
          }
        }
      }
    }
  }

  private fun resolveElement(destinationModuleClass: TypeScriptClass,
                             pair: com.intellij.openapi.util.Pair<ES6ImportPsiUtil.CreateImportExportInfo, PsiElement>): TypeScriptClass? {
    val info = pair.first
    val actualExportedName = info.exportedName ?: info.importedName
                             ?: return null
    val module = ES6PsiUtil.findExternalModule(pair.second, false) as? JSElement
                 ?: return null
    return ES6PsiUtil.resolveSymbolInModule(actualExportedName, destinationModuleClass, module)
      .firstNotNullOfOrNull { it.element as? TypeScriptClass }
  }

  class Angular2ComponentImportsTransferableData(list: ArrayList<ImportedElement>) : ES6ImportsTransferableDataBase(list) {
    override fun getFlavor(): DataFlavor {
      return ANGULAR2_COMPONENT_IMPORTS_FLAVOR
    }
  }

  companion object {
    private val ANGULAR2_COMPONENT_IMPORTS_FLAVOR = DataFlavor(Angular2ComponentImportsTransferableData::class.java,
                                                               "angular2 component imports")
  }
}
