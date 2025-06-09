// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types

import com.intellij.javascript.polySymbols.jsType
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.model.psi.PsiSymbolReferenceService
import com.intellij.polySymbols.js.JS_EVENTS
import com.intellij.polySymbols.PolySymbol
import org.angular2.web.NG_DIRECTIVE_OUTPUTS

class Angular2HostEventType : Angular2BaseType<JSProperty> {
  constructor(attribute: JSProperty) : super(attribute, JSProperty::class.java)
  private constructor(source: JSTypeSource) : super(source, JSProperty::class.java)

  override val typeOfText: String
    get() = "eventof#" + sourceElement.name

  override fun copyWithNewSource(source: JSTypeSource): JSType {
    return Angular2HostEventType(source)
  }

  override fun resolveType(context: JSTypeSubstitutionContext): JSType? {
    val property = sourceElement
    val references = PsiSymbolReferenceService.getService().getReferences(property)
    val eventType = references.asSequence()
      .flatMap { it.resolveReference() }
      .filterIsInstance<PolySymbol>()
      .find { it.qualifiedKind == JS_EVENTS || it.qualifiedKind == NG_DIRECTIVE_OUTPUTS }
      ?.jsType
    return eventType ?: JSAnyType.get(property)
  }
}