// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.WriteAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItemInsertHandler
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.entities.Angular2Component
import org.angular2.entities.Angular2Declaration
import org.angular2.entities.Angular2DirectiveSelector
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import org.angular2.lang.expr.psi.Angular2TemplateBindings

object Angular2CodeInsightUtils {

  @JvmStatic
  fun decorateCodeCompletionItem(item: WebSymbolCodeCompletionItem,
                                 declarations: List<Angular2Declaration>,
                                 proximity: DeclarationProximity,
                                 moduleScope: Angular2DeclarationsScope): WebSymbolCodeCompletionItem {
    if (proximity == DeclarationProximity.IMPORTABLE || proximity == DeclarationProximity.IN_SCOPE) {
      val modules = getModules(declarations, moduleScope)
      if (modules.size == 1) {
        return item.withTailText(" (" + modules[0].getName() + ")")
      }
    }
    return item
  }

  @JvmStatic
  fun wrapWithImportDeclarationModuleHandler(item: WebSymbolCodeCompletionItem,
                                             elementClass: Class<out PsiElement>): WebSymbolCodeCompletionItem {
    return item.withInsertHandlerAdded(object : WebSymbolCodeCompletionItemInsertHandler {
      override val priority: WebSymbol.Priority
        get() = WebSymbol.Priority.LOWEST

      override fun prepare(context: InsertionContext, item: LookupElement, completeAfterInsert: Boolean): Runnable? {
        val templateBindings = Angular2TemplateBindings::class.java == elementClass
        val elementPointer =
          PsiTreeUtil.getParentOfType<PsiElement>(
            context.file.findElementAt(context.startOffset),
            if (templateBindings) XmlAttribute::class.java else elementClass
          )
            ?.createSmartPointer()
          ?: return null
        return Runnable {
          WriteAction.run<RuntimeException> { PsiDocumentManager.getInstance(context.project).commitDocument(context.document) }
          var newElement = elementPointer.element ?: return@Runnable
          if (templateBindings && newElement is XmlAttribute) {
            newElement = Angular2TemplateBindings.get(newElement)
          }
          Angular2FixesFactory.ensureDeclarationResolvedAfterCodeCompletion(newElement, context.editor)
        }
      }
    })
  }

  @JvmStatic
  fun decorateLookupElementWithModuleSource(element: LookupElementBuilder,
                                            declarations: List<Angular2Declaration>,
                                            proximity: DeclarationProximity,
                                            moduleScope: Angular2DeclarationsScope): LookupElementBuilder {
    var result = element
    if (proximity == DeclarationProximity.IMPORTABLE) {
      val modules = getModules(declarations, moduleScope)
      if (modules.size == 1) {
        result = result.appendTailText(" (" + modules[0].getName() + ")", true)
      }
    }
    return result
  }

  @JvmStatic
  fun wrapWithImportDeclarationModuleHandler(element: LookupElementBuilder,
                                             elementClass: Class<out PsiElement>): LookupElementBuilder {
    val originalHandler = element.insertHandler
    return element.withInsertHandler(InsertHandler { context, item ->
      val templateBindings = Angular2TemplateBindings::class.java == elementClass
      val elementPointer = PsiTreeUtil.getParentOfType<PsiElement>(
        context.file.findElementAt(context.startOffset),
        if (templateBindings) XmlAttribute::class.java else elementClass
      )
        ?.createSmartPointer()
      originalHandler?.handleInsert(context, item)
      if (elementPointer == null) {
        return@InsertHandler
      }
      WriteAction.run<RuntimeException> { PsiDocumentManager.getInstance(context.project).commitDocument(context.document) }
      var elementToImport = elementPointer.element ?: return@InsertHandler
      if (templateBindings && elementToImport is XmlAttribute) {
        elementToImport = Angular2TemplateBindings.get(elementToImport)
      }
      Angular2FixesFactory.ensureDeclarationResolvedAfterCodeCompletion(elementToImport, context.editor)
    })
  }

  @JvmStatic
  fun getAvailableNgContentSelectorsSequence(xmlTag: XmlTag,
                                             scope: Angular2DeclarationsScope): Sequence<Angular2DirectiveSelector.SimpleSelectorWithPsi> {
    return xmlTag.parentTag
             ?.let { Angular2ApplicableDirectivesProvider(it).matched }
             ?.asSequence()
             ?.filter { it is Angular2Component && scope.contains(it) }
             ?.flatMap { (it as Angular2Component).ngContentSelectors }
             ?.flatMap { it.simpleSelectorsWithPsi }
           ?: emptySequence()
  }

  private fun getModules(declarations: List<Angular2Declaration>,
                         moduleScope: Angular2DeclarationsScope) =
    declarations.asSequence()
      .flatMap { declaration ->
        val sources = moduleScope.getPublicModulesExporting(declaration)
        val source = sources.find { module -> module.declarations.contains(declaration) }
        if (source != null) listOf(source) else sources
      }
      .distinct()
      .toList()
}
