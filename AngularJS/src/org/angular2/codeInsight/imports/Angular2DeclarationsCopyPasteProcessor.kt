// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.imports

import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.lang.Language
import com.intellij.lang.ecmascript6.editor.JSCopyPasteProcessorBase
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.asSafely
import com.intellij.xml.util.XmlTagUtil
import org.angular2.Angular2DecoratorUtil
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.entities.Angular2ComponentLocator
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.inspections.actions.NgModuleImportAction
import org.angular2.inspections.quickfixes.Angular2FixesPsiUtil
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.Angular2HtmlFile
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector
import java.awt.datatransfer.DataFlavor

class Angular2DeclarationsCopyPasteProcessor : JSCopyPasteProcessorBase<Angular2DeclarationsCopyPasteProcessor.Angular2DeclarationsImportsTransferableData>() {

  override val dataFlavor: DataFlavor
    get() = ANGULAR2_DECLARATIONS_IMPORTS_FLAVOR

  override fun isAcceptableCopyContext(file: PsiFile, contextElements: List<PsiElement>): Boolean {
    val settings = JSApplicationSettings.getInstance()
    return settings.isUseTypeScriptAutoImport
           && isAcceptablePasteContext(file) || contextElements.all { isAcceptablePasteContext(it) }
  }

  override fun isAcceptablePasteContext(context: PsiElement): Boolean =
    context.containingFile.let { it is Angular2HtmlFile || (it is JSFile && it.language == Angular2Language.INSTANCE) }

  override fun hasUnsupportedContentInCopyContext(parent: PsiElement, textRange: TextRange): Boolean =
    false

  override fun getExportScope(file: PsiFile, caret: Int): PsiElement? =
    Angular2ComponentLocator.findComponentClass(getContextElementOrFile(file, caret))?.containingFile

  override fun collectTransferableData(rangesWithParents: List<Pair<PsiElement, TextRange>>): Angular2DeclarationsImportsTransferableData? {
    val expressionContexts = rangesWithParents.count { Angular2ExpressionsCopyPasteProcessor.isExpressionContext(it.first) }
    if (expressionContexts != 0 && expressionContexts != rangesWithParents.size)
      return null
    val componentClass = rangesWithParents.getOrNull(0)?.first?.let { Angular2ComponentLocator.findComponentClass(it) }
                         ?: return null
    val scopeFile = PsiUtilCore.getVirtualFile(componentClass) ?: return null
    val originFilePath = scopeFile.path
    val exportName = componentClass.name ?: return null
    val pipes = mutableListOf<String>()
    val selectors = mutableListOf<Angular2DirectivesData>()

    rangesWithParents.forEach { (parent, range) ->
      parent.accept(object : XmlRecursiveElementWalkingVisitor() {
        override fun visitXmlTag(tag: XmlTag) {
          if (range.intersects(tag.textRange)) {
            val importElement = XmlTagUtil.getStartTagRange(tag)?.let { range.intersects(it) } == true
            val importAttributes = tag.attributes.mapNotNull { if (it.textRange.intersects(range)) it.name else null }
            selectors.add(
              Angular2DirectivesData(importElement, importAttributes, Angular2DirectiveSimpleSelector.createElementCssSelector(tag)))
          }
          super.visitXmlTag(tag)
        }

        override fun visitElement(element: PsiElement) {
          super.visitElement(element)
          if (element is Angular2PipeReferenceExpression) {
            if (element.textRange.intersects(range)) {
              pipes.add(element.referenceName!!)
            }
          }
          else if (element is Angular2TemplateBindings) {
            if (element.textRange.intersects(range)) {
              val selector = Angular2DirectiveSimpleSelector.createTemplateBindingsCssSelector(element)
              selectors.add(Angular2DirectivesData(false, selector.attrNames, selector))
            }
          }
        }
      })
    }
    if (pipes.isEmpty() && selectors.isEmpty()) return null
    return Angular2DeclarationsImportsTransferableData(originFilePath, exportName, pipes, selectors, expressionContexts != 0)
  }

  override fun processTransferableData(values: List<Angular2DeclarationsImportsTransferableData>,
                                       exportScope: PsiElement,
                                       pasteContext: PsiElement,
                                       pasteContextLanguage: Language) {
    val isExpressionContext = Angular2ExpressionsCopyPasteProcessor.isExpressionContext(pasteContext)
    val filteredValues = values.filter { it.isExpressionContext == isExpressionContext && (it.pipes.isNotEmpty() || it.directives.isNotEmpty()) }
    if (filteredValues.isEmpty())
      return
    val exportScopePtr = exportScope.createSmartPointer()
    val pasteContextPtr = pasteContext.createSmartPointer()
    runInBackground(pasteContext.project, Angular2Bundle.message("angular.progress.title.auto-importing-angular-directives-on-paste")) {
      restoreDeclarationsImports(filteredValues, exportScopePtr, pasteContextPtr)
    }
  }

  private fun restoreDeclarationsImports(values: List<Angular2DeclarationsImportsTransferableData>,
                                         exportScopePtr: SmartPsiElementPointer<PsiElement>,
                                         pasteContextPtr: SmartPsiElementPointer<PsiElement>) {
    val moduleClassesToImport = ReadAction.compute<List<Pair<String, SmartPsiElementPointer<PsiElement>>>, Throwable> {
      val exportScope = exportScopePtr.dereference() ?: return@compute emptyList()
      val pasteContext = pasteContextPtr.dereference() ?: return@compute emptyList()
      val resolveScope = JSResolveUtil.getResolveScope(exportScope)

      values.flatMap { data ->
        val sourceComponentFile = findFile(data.originFilePath, exportScope, resolveScope) as? JSFile
                                  ?: return@flatMap emptyList()
        val sourceComponentClass = ES6PsiUtil.resolveSymbolInModule(data.exportName, exportScope, sourceComponentFile)
                                     .firstNotNullOfOrNull { it.element.asSafely<TypeScriptClass>() }
                                   ?: return@flatMap emptyList()
        val sourceComponent = Angular2EntitiesProvider.getComponent(sourceComponentClass)
        val sourceScope = sourceComponent?.templateFile?.let { Angular2DeclarationsScope(it) }
                          ?: return@flatMap emptyList()

        val destinationScope = Angular2DeclarationsScope(pasteContext)

        val project = sourceComponentClass.project
        val pipesToImport = data.pipes.flatMap { pipeName ->
          Angular2EntitiesProvider.findPipes(project, pipeName)
            .filter { it in sourceScope && it !in destinationScope }
        }

        val directivesToImport = data.directives.flatMap { directivesData ->
          Angular2ApplicableDirectivesProvider(
            project, directivesData.selector.elementName ?: "", false, directivesData.selector
          )
            .matched
            .filter { it in sourceScope && it !in destinationScope && wasInCopyRange(it, directivesData) }
        }
        pipesToImport.asSequence()
          .plus(directivesToImport.asSequence())
          .mapNotNull {
            ProgressManager.checkCanceled()
            NgModuleImportAction.declarationsToModuleImports(pasteContext, listOf(it), destinationScope)
              .firstOrNull()
          }
          .distinct()
          .sortedBy { it.name }
          .mapNotNull { Pair(it.name, it.element?.createSmartPointer() ?: return@mapNotNull null) }
          .toList()
      }
    }
    if (moduleClassesToImport.isNotEmpty()) {
      WriteAction.runAndWait<Throwable> {
        val pasteContext = pasteContextPtr.dereference()
                           ?: return@runAndWait
        val scope = Angular2DeclarationsScope(pasteContext)
        val importsOwner = scope.importsOwner
        val destinationModuleClass = importsOwner?.typeScriptClass
                                     ?: return@runAndWait
        val project = pasteContext.project
        CommandProcessor.getInstance().executeCommand(
          project,
          {
            for ((name, elementPtr) in moduleClassesToImport) {
              val element = elementPtr.dereference() ?: continue
              ES6ImportPsiUtil.insertJSImport(destinationModuleClass, name, element, null)
              Angular2FixesPsiUtil.insertEntityDecoratorMember(importsOwner, Angular2DecoratorUtil.IMPORTS_PROP, name)
            }
          },
          Angular2Bundle.message("angular.command.name.auto-import-angular-directives"),
          null,
          UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION,
          PsiDocumentManager.getInstance(project).getDocument(pasteContext.containingFile)
        )

      }
    }
  }

  private fun wasInCopyRange(directive: Angular2Directive, directivesData: Angular2DirectivesData): Boolean {
    val selector = directive.selector
    val elementName = directivesData.selector.elementName.takeIf { directivesData.importElement }
    val attributes = directivesData.importAttributes.toSet()
    return selector.simpleSelectors.any { simpleSelector ->
      simpleSelector.elementName.let { it != null && it == elementName }
      || simpleSelector.attrNames.any { it in attributes }
    }
  }

  data class Angular2DeclarationsImportsTransferableData(
    val originFilePath: String,
    val exportName: String,
    val pipes: List<String>,
    val directives: List<Angular2DirectivesData>,
    val isExpressionContext: Boolean,
  ) : TextBlockTransferableData {
    override fun getFlavor(): DataFlavor {
      return ANGULAR2_DECLARATIONS_IMPORTS_FLAVOR
    }
  }

  data class Angular2DirectivesData(
    val importElement: Boolean,
    val importAttributes: List<String>,
    val selector: Angular2DirectiveSimpleSelector
  )

  companion object {
    private val ANGULAR2_DECLARATIONS_IMPORTS_FLAVOR = DataFlavor(Angular2DeclarationsImportsTransferableData::class.java,
                                                                  "angular2 declarations imports")
  }
}
