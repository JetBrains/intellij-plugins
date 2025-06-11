// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.html.polySymbols.PolySymbolsHtmlQueryConfigurator
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.context.PolyContext
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.html.HTML_ELEMENTS
import com.intellij.polySymbols.html.NAMESPACE_HTML
import com.intellij.polySymbols.query.PolySymbolsQueryResultsCustomizer
import com.intellij.polySymbols.query.PolySymbolsQueryResultsCustomizerFactory
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.nameSegments
import com.intellij.polySymbols.webTypes.WebTypesSymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import org.jetbrains.vuejs.codeInsight.detectVueScriptLanguage
import org.jetbrains.vuejs.codeInsight.tags.VueInsertHandler
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.symbols.VuePolyTypesMergedSymbol

class VuePolySymbolsQueryResultsCustomizer(private val context: PsiElement) : PolySymbolsQueryResultsCustomizer {

  private val scriptLanguage by lazy(LazyThreadSafetyMode.PUBLICATION) {
    detectVueScriptLanguage(context.containingFile)
  }

  override fun createPointer(): Pointer<out PolySymbolsQueryResultsCustomizer> {
    val contextPtr = context.createSmartPointer()
    return Pointer {
      contextPtr.dereference()?.let { VuePolySymbolsQueryResultsCustomizer(it) }
    }
  }

  override fun apply(
    matches: List<PolySymbol>,
    strict: Boolean,
    qualifiedName: PolySymbolQualifiedName,
  ): List<PolySymbol> {
    if (qualifiedName.namespace != NAMESPACE_HTML) return matches

    var result = matches
    if (qualifiedName.matches(VUE_COMPONENTS)) {
      if (result.size > 1) {
        val mergedSymbol = result.find { it is VuePolyTypesMergedSymbol } as? VuePolyTypesMergedSymbol
        if (mergedSymbol != null) {
          val mergedWebTypes = mergedSymbol.webTypesSymbols
          // The check can get very expensive with more web-types merged
          // Limit checks to some reasonable sizes. Usually it should be only one merged.
          if (mergedWebTypes.size < 5) {
            result = result.filter { it !is WebTypesSymbol || mergedWebTypes.none { merged -> merged.isEquivalentTo(it) } }
          }
        }
      }
      if (!strict) return result
    }
    else if (qualifiedName.matches(HTML_ELEMENTS)) {
      val standardHtmlSymbols = result.filterTo(LinkedHashSet()) { symbol ->
        symbol.nameSegments.flatMap { it.symbols }.any { it is PolySymbolsHtmlQueryConfigurator.StandardHtmlSymbol }
      }
      if (standardHtmlSymbols.isEmpty()) return result
      if (isVueComponentQuery(qualifiedName)) {
        return result.filter { it !in standardHtmlSymbols }
      }
    }

    return result.filter { symbol ->
      symbol[PROP_VUE_PROXIMITY] != VueModelVisitor.Proximity.OUT_OF_SCOPE ||
      symbol[PROP_VUE_COMPOSITION_COMPONENT] == true
    }
  }

  override fun apply(
    item: PolySymbolCodeCompletionItem,
    qualifiedKind: PolySymbolQualifiedKind,
  ): PolySymbolCodeCompletionItem {
    when (qualifiedKind) {
      VUE_COMPONENTS -> {
        val proximity = item.symbol?.get(PROP_VUE_PROXIMITY)
        val element = (item.symbol as? PsiSourcedPolySymbol)?.source
        if (proximity == VueModelVisitor.Proximity.OUT_OF_SCOPE && element != null) {
          val settings = JSApplicationSettings.getInstance()
          if ((scriptLanguage != null && "ts" == scriptLanguage)
              || (DialectDetector.isTypeScript(element)
                  && !JSLibraryUtil.isProbableLibraryFile(element.containingFile.viewProvider.virtualFile))) {
            if (settings.hasTSImportCompletionEffective(element.project)) {
              return item.withInsertHandlerAdded(VueInsertHandler.INSTANCE, PolySymbol.Priority.LOWEST)
            }
          }
          else {
            if (settings.isUseJavaScriptAutoImport) {
              return item.withInsertHandlerAdded(VueInsertHandler.INSTANCE, PolySymbol.Priority.LOWEST)
            }
          }
        }
      }
      HTML_ATTRIBUTES -> {
        if (item.name.let { it.startsWith("v-") && it.contains(":") }) {
          return item.withProximity((item.proximity ?: 0) + 5)
        }
        else if (item.name.let { it.startsWith("#") || it.startsWith(":") || it.startsWith("@") }) {
          return item.withProximity((item.proximity ?: 0) + 10)
        }
      }
    }
    return item
  }

  override fun getModificationCount(): Long = 0

  override fun equals(other: Any?): Boolean =
    other is VuePolySymbolsQueryResultsCustomizer
    && other.context == context

  override fun hashCode(): Int =
    context.hashCode()

  class Provider : PolySymbolsQueryResultsCustomizerFactory {
    override fun create(location: PsiElement, context: PolyContext): PolySymbolsQueryResultsCustomizer? =
      if (context.framework == VueFramework.ID)
        VuePolySymbolsQueryResultsCustomizer(location)
      else null

  }

}