// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.references

import com.intellij.javascript.webSymbols.symbols.asWebSymbol
import com.intellij.javascript.webSymbols.symbols.getMatchingJSPropertySymbols
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.util.stubSafeStringValue
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.references.PsiWebSymbolReferenceProvider
import org.angular2.Angular2DecoratorUtil.INPUTS_PROP
import org.angular2.Angular2DecoratorUtil.OUTPUTS_PROP
import org.angular2.entities.Angular2ClassBasedDirective
import org.angular2.entities.Angular2EntityUtils.getPropertyDeclarationOrReferenceKindAndDirective
import org.angular2.web.NG_DIRECTIVE_INPUTS
import org.angular2.web.NG_DIRECTIVE_OUTPUTS

class Angular2DirectivePropertyLiteralReferencesProvider : PsiWebSymbolReferenceProvider<JSLiteralExpression> {

  override fun getOffsetsToReferencedSymbols(psiElement: JSLiteralExpression): Map<Int, WebSymbol> {
    val stringValue = psiElement.stubSafeStringValue ?: return emptyMap()
    val colonIndex = stringValue.indexOf(':').takeIf { it >= 0 } ?: stringValue.length
    val startOffset = StringUtil.skipWhitespaceForward(stringValue, 0)
    val endOffset = StringUtil.skipWhitespaceBackward(stringValue, colonIndex)
    if (startOffset >= endOffset)
      return emptyMap()

    val (kind, directive, hostDirective) = getPropertyDeclarationOrReferenceKindAndDirective(psiElement, false)
                                           ?: return emptyMap()
    if (kind != INPUTS_PROP && kind != OUTPUTS_PROP)
      return emptyMap()

    val name = stringValue.substring(startOffset, endOffset)

    return JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(psiElement) {
      if (hostDirective) {
        val properties = (if (kind == INPUTS_PROP) directive.inputs else directive.outputs)
        val symbol = properties.find { it.name == name }
                     ?: PsiWebSymbolReferenceProvider.unresolvedSymbol(if (kind == INPUTS_PROP) NG_DIRECTIVE_INPUTS else NG_DIRECTIVE_OUTPUTS, name)
        mapOf(startOffset + 1 to symbol)
      }
      else {
        val symbol = directive
                       .asSafely<Angular2ClassBasedDirective>()
                       ?.typeScriptClass
                       ?.asWebSymbol()
                       ?.getMatchingJSPropertySymbols(name, null)
                       ?.find { it.source is JSAttributeListOwner }
                     ?: return@withTypeEvaluationLocation emptyMap()
        mapOf(startOffset + 1 to symbol)
      }
    }
  }

}