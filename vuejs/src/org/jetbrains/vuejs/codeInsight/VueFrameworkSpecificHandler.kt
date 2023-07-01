// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight

import com.intellij.html.webSymbols.attributes.WebSymbolAttributeDescriptor
import com.intellij.javascript.webSymbols.jsType
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandler
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.context.hasPinia
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.lang.expr.VueExprMetaLanguage
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.evaluateInjectedType
import org.jetbrains.vuejs.model.findInjectForCall
import org.jetbrains.vuejs.model.provides
import org.jetbrains.vuejs.model.source.INJECT_FUN

class VueFrameworkSpecificHandler : JSFrameworkSpecificHandler {
  override fun useMoreAccurateEvaluation(context: PsiElement): Boolean =
    hasPinia(context)

  override fun findExpectedType(element: PsiElement, parent: PsiElement?, expectedTypeKind: JSExpectedTypeKind): JSType? {
    if (DialectDetector.isJavaScript(element) &&
        element is JSCallExpression &&
        VueFrameworkHandler.getFunctionNameFromVueIndex(element) == INJECT_FUN) {
      return getInjectType(element)
    }

    if (isTopmostVueExpression(element, parent)) {
      val attribute = element.parentOfTypeInAttribute<XmlAttribute>() ?: return null
      val attributeInfo = VueAttributeNameParser.parse(attribute.name, attribute.parent)

      if (attributeInfo is VueAttributeNameParser.VueDirectiveInfo &&
          attributeInfo.directiveKind == VueAttributeNameParser.VueDirectiveKind.ON) {
        return if (isMethodHandler(element)) getWebSymbolType(attribute) else null
      }
    }

    return null
  }

  private fun getInjectType(call: JSCallExpression): JSType? {
    val component = VueModelManager.findEnclosingComponent(call) ?: return null
    val inject = findInjectForCall(call, component) ?: return null
    return evaluateInjectedType(inject, component.global.provides)
  }

  private fun isTopmostVueExpression(element: PsiElement, parent: PsiElement?) =
    VueExprMetaLanguage.matches(element.language) &&
    parent is JSExpressionStatement &&
    JSStubBasedPsiTreeUtil.getParentOrNull(parent) is VueJSEmbeddedExpressionContent

  private fun isMethodHandler(element: PsiElement): Boolean {
    return element is JSReferenceExpression ||
           element is JSIndexedPropertyAccessExpression ||
           element is JSFunctionExpression
  }

  private fun getWebSymbolType(attribute: XmlAttribute) =
    attribute.descriptor.asSafely<WebSymbolAttributeDescriptor>()?.symbol?.jsType
}