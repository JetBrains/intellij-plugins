// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.completion.JSLookupPriority.*
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItemCustomizer
import com.intellij.polySymbols.context.PolyContext
import com.intellij.polySymbols.framework.framework
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.html.HTML_ELEMENTS
import com.intellij.polySymbols.js.JS_EVENTS
import com.intellij.polySymbols.js.JS_SYMBOLS
import com.intellij.polySymbols.js.decorateWithSymbolType
import com.intellij.polySymbols.js.toSymbolPriority
import com.intellij.psi.PsiElement
import com.intellij.psi.util.contextOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import org.jetbrains.vuejs.index.isScriptSetupTag
import org.jetbrains.vuejs.lang.html.isVueFileName
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueModelVisitor

class VueCodeCompletionItemCustomizer :
  PolySymbolCodeCompletionItemCustomizer {

  override fun customize(
    item: PolySymbolCodeCompletionItem,
    context: PolyContext,
    kind: PolySymbolKind,
    location: PsiElement,
  ): PolySymbolCodeCompletionItem? =
    if (context.framework == VueFramework.ID)
      when (kind) {
        HTML_ATTRIBUTES ->
          item.symbol
            ?.takeIf { it.kind == VUE_COMPONENT_PROPS || it.kind == JS_EVENTS }
            ?.let { item.decorateWithSymbolType(location, it) }
          ?: item
        HTML_ELEMENTS ->
          item.takeIf { !shouldFilterOutLowerCaseScriptSetupIdentifier(it) }
        JS_SYMBOLS ->
          item.let {
            val vueProximity = it.symbol?.get(PROP_VUE_PROXIMITY)
            if (vueProximity != null)
              it.withPriority(getJSLookupPriorityOf(vueProximity).toSymbolPriority(isJsSymbolOrProperty = true))
            else if (it.name.startsWith('$'))
              it.withPriority(LOCAL_SCOPE_MAX_PRIORITY_EXOTIC.toSymbolPriority(isJsSymbolOrProperty = true))
            else
              it
          }
        else -> item
      }
    else item

  private fun getJSLookupPriorityOf(proximity: VueModelVisitor.Proximity): JSLookupPriority =
    when (proximity) {
      VueModelVisitor.Proximity.LOCAL -> LOCAL_SCOPE_MAX_PRIORITY
      VueModelVisitor.Proximity.APP -> NESTING_LEVEL_1
      VueModelVisitor.Proximity.LIBRARY -> NESTING_LEVEL_2
      VueModelVisitor.Proximity.GLOBAL -> NESTING_LEVEL_3
      else -> LOWEST_PRIORITY
    }

  private fun shouldFilterOutLowerCaseScriptSetupIdentifier(item: PolySymbolCodeCompletionItem): Boolean {
    val source = item.symbol.asSafely<VueComponent>()?.source?.asSafely<JSPsiNamedElementBase>()
    if (source?.contextOfType<XmlTag>(false)?.isScriptSetupTag() != true)
      return false
    val originalName = if (source is ES6ImportSpecifier) source.declaredName else source.name
    return originalName?.getOrNull(0)?.isLowerCase() == true
           && (source !is ES6ImportedBinding
               || source.declaration
                 ?.fromClause?.referenceText?.let { JSStringUtil.unquoteStringLiteralValue(it) }
                 ?.let { isVueFileName(it) } != true)
  }
}