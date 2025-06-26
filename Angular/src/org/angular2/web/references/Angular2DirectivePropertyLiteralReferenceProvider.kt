// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.references

import com.intellij.javascript.JSBuiltInTypeEngineEvaluation
import com.intellij.polySymbols.js.symbols.asJSSymbol
import com.intellij.polySymbols.js.symbols.getMatchingJSPropertySymbols
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.util.stubSafeStringValue
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.asSafely
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.references.PsiPolySymbolReferenceProvider
import org.angular2.Angular2DecoratorUtil.INPUTS_PROP
import org.angular2.Angular2DecoratorUtil.OUTPUTS_PROP
import org.angular2.entities.Angular2ClassBasedDirective
import org.angular2.entities.Angular2EntityUtils.getPropertyDeclarationOrReferenceKindAndDirective
import org.angular2.web.NG_DIRECTIVE_INPUTS
import org.angular2.web.NG_DIRECTIVE_OUTPUTS

class Angular2DirectivePropertyLiteralReferenceProvider : PsiPolySymbolReferenceProvider<JSLiteralExpression> {

  override fun getOffsetsToReferencedSymbols(psiElement: JSLiteralExpression, hints: PsiSymbolReferenceHints): Map<Int, PolySymbol> {
    val stringValue = psiElement.stubSafeStringValue ?: return emptyMap()
    val colonIndex = stringValue.indexOf(':').takeIf { it >= 0 } ?: stringValue.length
    val startOffset = StringUtil.skipWhitespaceForward(stringValue, 0)
    val endOffset = StringUtil.skipWhitespaceBackward(stringValue, colonIndex)
    if (startOffset >= endOffset)
      return emptyMap()

    return JSBuiltInTypeEngineEvaluation.forceBuiltInTypeEngineIfNeeded(psiElement, hints) {
      map(psiElement, stringValue, startOffset, endOffset)
    }
  }

  private fun map(psiElement: JSLiteralExpression, stringValue: String, startOffset: Int, endOffset: Int): Map<Int, PolySymbol> {
    val (kind, directive, hostDirective) = getPropertyDeclarationOrReferenceKindAndDirective(psiElement, false)
                                           ?: return emptyMap()
    if (kind != INPUTS_PROP && kind != OUTPUTS_PROP)
      return emptyMap()

    val name = stringValue.substring(startOffset, endOffset)

    return if (hostDirective) {
      val properties = (if (kind == INPUTS_PROP) directive.inputs else directive.outputs)
      val symbol = properties.find { it.name == name }
                   ?: PsiPolySymbolReferenceProvider.unresolvedSymbol(if (kind == INPUTS_PROP) NG_DIRECTIVE_INPUTS else NG_DIRECTIVE_OUTPUTS, name)
      mapOf(startOffset + 1 to symbol)
    }
    else {
      val symbol = directive
                     .asSafely<Angular2ClassBasedDirective>()
                     ?.typeScriptClass
                     ?.asJSSymbol()
                     ?.getMatchingJSPropertySymbols(name, null)
                     ?.find { it.source is JSAttributeListOwner }
                   ?: return emptyMap()
      mapOf(startOffset + 1 to symbol)
    }
  }
}