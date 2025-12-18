// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.lang.javascript.psi.types.JSTypeSubstitutor
import com.intellij.openapi.util.text.Strings
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.documentation.PolySymbolDocumentation
import com.intellij.polySymbols.documentation.PolySymbolDocumentationCustomizer
import com.intellij.polySymbols.html.HTML_SLOTS
import com.intellij.polySymbols.js.JS_EVENTS
import com.intellij.polySymbols.js.renderJsTypeForDocs
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.expr.psi.impl.VueJSEmbeddedExpressionContentImpl

class VueDocumentationCustomizer : PolySymbolDocumentationCustomizer {
  override fun customize(symbol: PolySymbol, location: PsiElement?, documentation: PolySymbolDocumentation): PolySymbolDocumentation {
    if (symbol.kind == HTML_SLOTS
        && (symbol.origin.framework == VueFramework.ID
            || symbol.psiContext.let { it != null && isVueContext(it) })) {
      symbol.renderJsTypeForDocs(null, location)
        ?.replace(",", ",<br>")
        ?.let {
          @Suppress("HardCodedStringLiteral")
          return documentation.withDescriptionSection(
            VueBundle.message("vue.documentation.section.slot.scope"),
            "<code>$it</code>"
          )
        }
    }
    else if (symbol.kind == JS_EVENTS
             && (symbol.origin.framework == VueFramework.ID
                 || symbol.psiContext.let { it != null && isVueContext(it) })) {
      symbol.renderJsTypeForDocs(Strings.escapeXmlEntities(symbol.name), location, getTypeSubstitutorFor(location))?.let {
        return documentation.withDefinition(it)
      }
    }
    else {
      if (symbol.kind == VUE_COMPONENT_PROPS) {
        symbol.renderJsTypeForDocs(Strings.escapeXmlEntities(symbol.name), location, getTypeSubstitutorFor(location))?.let {
          return documentation.withDefinition(it)
        }
      }
    }
    return documentation
  }

  private fun getTypeSubstitutorFor(context: PsiElement?): JSTypeSubstitutor? =
    context?.parentOfType<XmlTag>()?.let {
      VueJSEmbeddedExpressionContentImpl.getTypeSubstitutorForGenerics(it)
    }

}