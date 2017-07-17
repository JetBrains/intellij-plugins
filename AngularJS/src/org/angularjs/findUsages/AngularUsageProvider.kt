package org.angularjs.findUsages

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class AngularUsageProvider : ImplicitUsageProvider {
  override fun isImplicitUsage(element: PsiElement?): Boolean {
    if (element is JSAttributeListOwner && element.attributeList != null) {
      element.attributeList!!.children
        .filterIsInstance<ES6Decorator>()
        .map { PsiTreeUtil.findChildOfType(it, JSCallExpression::class.java) }
        .map { it?.methodExpression }
        .filter {
          JSSymbolUtil.isAccurateReferenceExpressionName(it, "HostListener") ||
          JSSymbolUtil.isAccurateReferenceExpressionName(it, "HostBinding")
        }
        .forEach { return true }
    }
    return false
  }

  override fun isImplicitRead(element: PsiElement?) = false

  override fun isImplicitWrite(element: PsiElement?) = false
}