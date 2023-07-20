// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.references

import com.intellij.javascript.webSymbols.symbols.asWebSymbol
import com.intellij.javascript.webSymbols.symbols.getJSPropertySymbols
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.util.stubSafeStringValue
import com.intellij.model.Symbol
import com.intellij.model.search.SearchRequest
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.references.WebSymbolReferenceProvider
import org.angular2.Angular2DecoratorUtil.INPUTS_PROP
import org.angular2.entities.Angular2EntityUtils.getPropertyDeclarationOrReferenceKindAndDirective
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_INPUTS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_OUTPUTS

class Angular2DirectivePropertyLiteralReferencesProvider : WebSymbolReferenceProvider<JSLiteralExpression>() {

  override fun getOffsetsToSymbols(psiElement: JSLiteralExpression): Map<Int, WebSymbol> {
    val stringValue = psiElement.stubSafeStringValue ?: return emptyMap()
    val colonIndex = stringValue.indexOf(':').takeIf { it >= 0 } ?: stringValue.length
    val startOffset = StringUtil.skipWhitespaceForward(stringValue, 0)
    val endOffset = StringUtil.skipWhitespaceBackward(stringValue, colonIndex)
    if (startOffset >= endOffset)
      return emptyMap()

    val (kind, directive, hostDirective) = getPropertyDeclarationOrReferenceKindAndDirective(psiElement, false)
                                           ?: return emptyMap()
    val name = stringValue.substring(startOffset, endOffset)

    if (hostDirective) {
      val properties = (if (kind == INPUTS_PROP) directive.inputs else directive.outputs)
      val symbol = properties.find { it.name == name }
                   ?: unresolvedSymbol(NAMESPACE_JS, if (kind == INPUTS_PROP) KIND_NG_DIRECTIVE_INPUTS else KIND_NG_DIRECTIVE_OUTPUTS, name)
      return mapOf(startOffset + 1 to symbol)
    }
    else {
      val symbol = directive.typeScriptClass
                     ?.asWebSymbol()
                     ?.getJSPropertySymbols(name)
                     ?.find { it.source is JSAttributeListOwner }
                   ?: return emptyMap()
      return mapOf(startOffset + 1 to symbol)
    }
  }

  override fun getSearchRequests(project: Project, target: Symbol): Collection<SearchRequest> =
    emptyList()

}