// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.scopes

import com.intellij.javascript.webSymbols.symbols.asWebSymbol
import com.intellij.model.Pointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScopeWithCache
import org.angular2.codeInsight.blocks.getDeferOnTriggerDefinition
import org.angular2.codeInsight.template.Angular2TemplateScopesResolver
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable

class DeferOnTriggerParameterScope(parameter: Angular2BlockParameter) :
  WebSymbolsScopeWithCache<Angular2BlockParameter, Unit>(null, parameter.project, parameter, Unit) {

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
    val names = mutableSetOf<String>()
    val trigger = getDeferOnTriggerDefinition(dataHolder)
    if (trigger?.properties?.get("parameter") == "template-reference-variable") {
      Angular2TemplateScopesResolver.resolve(dataHolder) { resolve ->
        resolve.element?.takeIf { resolve.isValidResult }
          ?.asSafely<Angular2HtmlAttrVariable>()
          ?.takeIf { it.kind == Angular2HtmlAttrVariable.Kind.REFERENCE && it.name.let { name -> name != null && names.add(name) } }
          ?.asWebSymbol()
          ?.let(consumer)
        true
      }
    }
  }

  override fun isExclusiveFor(qualifiedKind: WebSymbolQualifiedKind): Boolean {
    return qualifiedKind == WebSymbol.JS_SYMBOLS
  }

  override fun createPointer(): Pointer<DeferOnTriggerParameterScope> {
    val context = dataHolder.createSmartPointer()
    return Pointer {
      context.dereference()?.let { DeferOnTriggerParameterScope(it) }
    }
  }

}