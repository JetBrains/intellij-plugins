// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.documentation.JSDocumentationUtils
import com.intellij.lang.javascript.highlighting.JSSemanticHighlightingVisitor
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.psi.PsiElement
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.expr.highlighting.Angular2HighlighterColors
import org.angular2.lang.expr.psi.impl.Angular2TemplateVariableImpl
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable
import org.angular2.signals.Angular2SignalUtils

class Angular2SpecialHighlightingAnnotator : Annotator {

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    element.accept(object : JSElementVisitor() {
      override fun visitJSReferenceExpression(node: JSReferenceExpression) {
        if (isAcceptableReferenceForHighlighting(node)) {
          highlight(node, holder)
        }
      }

      override fun visitElement(element: PsiElement) {
        val elementType = element.getNode().getElementType()
        if (JSKeywordSets.IDENTIFIER_NAMES.contains(elementType)) {
          val namedElement = JSPsiImplUtils.findElementFromNameIdentifier(element)
          if (namedElement is JSVariable || namedElement is JSProperty) {
            highlight(namedElement, holder)
          }
        }
      }
    })
  }

  private fun highlight(element: PsiElement, holder: AnnotationHolder) {
    if (Angular2LangUtil.isAngular2Context(element) && Angular2SignalUtils.isSignal(element)) {
      JSSemanticHighlightingVisitor.lineMarker(element, Angular2HighlighterColors.NG_SIGNAL, "ng-signal", holder)
    } else {
      val resolved = (element as? JSReferenceExpression)?.resolve() ?: element
      if (resolved is Angular2TemplateVariableImpl || resolved is Angular2HtmlAttrVariable) {
        JSSemanticHighlightingVisitor.lineMarker(element, Angular2HighlighterColors.NG_VARIABLE, "ng-variable", holder)
      }
    }
  }

  private fun isAcceptableReferenceForHighlighting(node: JSReferenceExpression): Boolean {
    val parent = node.getParent()
    if (parent is ES6Decorator || parent is JSCallExpression && parent.getParent() is ES6Decorator) {
      return false
    }
    return if (node.getQualifier() != null) true
    else node.getNode().findChildByType(JSDocumentationUtils.ourPrimitiveTypeFilter) == null ||
         !JSResolveUtil.isExprInTypeContext(node)
  }

}