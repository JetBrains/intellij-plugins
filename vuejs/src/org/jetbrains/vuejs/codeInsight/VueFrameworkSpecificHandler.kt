// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight

import com.intellij.html.polySymbols.attributes.HtmlAttributeSymbolDescriptor
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.polySymbols.jsType
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandler
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.lang.javascript.psi.types.JSTypeContext
import com.intellij.lang.javascript.psi.types.JSTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.lang.javascript.psi.types.primitives.JSStringType
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.asSafely
import com.intellij.xml.util.HtmlUtil.SLOT_TAG_NAME
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.context.hasPinia
import org.jetbrains.vuejs.index.getFunctionNameFromVueIndex
import org.jetbrains.vuejs.lang.expr.isVueExprMetaLanguage
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.INJECT_FUN

class VueFrameworkSpecificHandler : JSFrameworkSpecificHandler {
  override fun useMoreAccurateEvaluation(context: PsiElement): Boolean = hasPinia(context)

  override fun shouldPreserveAlias(type: JSType): Boolean {
    return type is JSTypeImpl && type.typeText == "DefineProps"
  }

  override fun findExpectedType(element: PsiElement, parent: PsiElement?, expectedTypeKind: JSExpectedTypeKind): JSType? {
    if (DialectDetector.isJavaScript(element) &&
        element is JSCallExpression &&
        isInjectCall(element)) {
      return getInjectType(element)
    }

    if (isTopmostVueExpression(element, parent)) {
      val attribute = element.parentOfTypeInAttribute<XmlAttribute>() ?: return null
      val tag = attribute.parent
      val attributeInfo = VueAttributeNameParser.parse(attribute.name, tag)
      val tagName = tag.name

      if (attributeInfo is VueAttributeNameParser.VueDirectiveInfo &&
          attributeInfo.directiveKind == VueAttributeNameParser.VueDirectiveKind.ON) {
        return if (isMethodHandler(element)) getPolySymbolType(attribute) else null
      }
      if (tagName == SLOT_TAG_NAME &&
          attributeInfo is VueAttributeNameParser.VueDirectiveInfo &&
          attributeInfo.directiveKind == VueAttributeNameParser.VueDirectiveKind.BIND &&
          attributeInfo.arguments == SLOT_NAME_ATTRIBUTE) {
        return JSStringType(true, JSTypeSourceFactory.createTypeSource(element, true), JSTypeContext.INSTANCE)
      }
    }

    return null
  }

  private fun isInjectCall(element: JSCallExpression): Boolean =
    element
      .takeIf { getFunctionNameFromVueIndex(it) == INJECT_FUN }
      ?.methodExpression.asSafely<JSReferenceExpression>()
      ?.resolve()
      ?.containingFile?.virtualFile
      ?.let { NodeModuleSearchUtil.findDependencyRoot(it) }
      ?.let { it.name == "vue" || it.parent?.name == "@vue" }
    ?: false

  private fun getInjectType(call: JSCallExpression): JSType? {
    val component = VueModelManager.findEnclosingComponent(call) ?: return null
    val inject = findInjectForCall(call, component) ?: return null
    return evaluateInjectedType(inject, component.global.provides)
  }

  private fun isTopmostVueExpression(element: PsiElement, parent: PsiElement?) =
    isVueExprMetaLanguage(element.language) &&
    ((parent is JSExpressionStatement && JSStubBasedPsiTreeUtil.getParentOrNull(parent) is VueJSEmbeddedExpressionContent) ||
     (element is JSStringTemplateExpression && parent is VueJSEmbeddedExpressionContent))

  private fun isMethodHandler(element: PsiElement): Boolean {
    return element is JSReferenceExpression ||
           element is JSIndexedPropertyAccessExpression ||
           element is JSFunctionExpression
  }

  private fun getPolySymbolType(attribute: XmlAttribute) =
    attribute.descriptor.asSafely<HtmlAttributeSymbolDescriptor>()?.symbol?.jsType
}