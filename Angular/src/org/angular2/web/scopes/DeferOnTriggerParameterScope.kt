// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.scopes

import com.intellij.javascript.polySymbols.symbols.asPolySymbol
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.js.JS_KEYWORDS
import com.intellij.polySymbols.js.JS_SYMBOLS
import com.intellij.polySymbols.utils.PolySymbolsScopeWithCache
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.angular2.codeInsight.blocks.PROP_PARAMETER
import org.angular2.codeInsight.blocks.getDeferOnTriggerDefinition
import org.angular2.codeInsight.template.Angular2TemplateScopesResolver
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable

class DeferOnTriggerParameterScope(parameter: Angular2BlockParameter) :
  PolySymbolsScopeWithCache<Angular2BlockParameter, Unit>(null, parameter.project, parameter, Unit) {

  override fun provides(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    qualifiedKind == JS_SYMBOLS

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
    val names = mutableSetOf<String>()
    val trigger = getDeferOnTriggerDefinition(dataHolder)
    if (trigger?.get(PROP_PARAMETER) == "template-reference-variable") {
      Angular2TemplateScopesResolver.resolve(dataHolder) { resolve ->
        resolve.element?.takeIf { resolve.isValidResult }
          ?.asSafely<Angular2HtmlAttrVariable>()
          ?.takeIf { it.kind == Angular2HtmlAttrVariable.Kind.REFERENCE && it.name.let { name -> name != null && names.add(name) } }
          ?.asPolySymbol()
          ?.let(consumer)
        true
      }
    }
  }

  override fun isExclusiveFor(qualifiedKind: PolySymbolQualifiedKind): Boolean {
    return qualifiedKind == JS_SYMBOLS || qualifiedKind == JS_KEYWORDS
  }

  override fun createPointer(): Pointer<DeferOnTriggerParameterScope> {
    val context = dataHolder.createSmartPointer()
    return Pointer {
      context.dereference()?.let { DeferOnTriggerParameterScope(it) }
    }
  }
}