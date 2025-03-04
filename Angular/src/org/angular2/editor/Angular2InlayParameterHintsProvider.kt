// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor

import com.intellij.codeInsight.hints.InlayInfo
import com.intellij.codeInsight.hints.Option
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSCallLikeExpression
import com.intellij.lang.javascript.psi.JSParameterItem
import com.intellij.lang.typescript.editing.TypeScriptInlayParameterHintsProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.html.HtmlDocumentImpl
import com.intellij.psi.impl.source.html.HtmlFileImpl
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.psi.Angular2Interpolation
import org.angular2.lang.expr.psi.Angular2PipeExpression

class Angular2InlayParameterHintsProvider : TypeScriptInlayParameterHintsProvider() {

  override fun getShowNameForLiteralArgsOption(): Option {
    return Options.NAMES_FOR_LITERAL_ARGS
  }

  override fun getShowNameForAllArgsOption(): Option {
    return Options.NAMES_FOR_ALL_ARGS
  }

  override fun getSupportedOptions(): List<Option> {
    return listOf(showNameForLiteralArgsOption, showNameForAllArgsOption, Options.NAMES_FOR_PIPES)
  }

  override fun shouldInlineParameterName(argument: PsiElement, parameter: JSParameterItem, callExpression: JSCallLikeExpression): Boolean =
    if (callExpression is Angular2PipeExpression)
      Options.NAMES_FOR_PIPES.get()
    else
      super.shouldInlineParameterName(argument, parameter, callExpression)

  override fun skipIndex(i: Int, expression: JSCallLikeExpression): Boolean {
    return (expression is Angular2PipeExpression && i == 0) || super.skipIndex(i, expression)
  }

  override fun getParameterHints(element: PsiElement): List<InlayInfo> {
    if (element is JSCallExpression && isSettingsPreview(element)) {
      if (element.text == "foo('phone', 22)")
        return createFixedHints(element, "a", "b")
      if (element.text == "foo(phone, 22)")
        return createFixedHints(element, "a")
    }

    return super.getParameterHints(element)
  }

  object Options {
    val NAMES_FOR_LITERAL_ARGS: Option = Option(
      "angular.show.names.for.literal.args", JavaScriptBundle.messagePointer("js.param.hints.show.names.for.literal.args"), true)
    val NAMES_FOR_ALL_ARGS: Option = Option(
      "angular.show.names.for.all.args", JavaScriptBundle.messagePointer("js.param.hints.show.names.for.all.args"), false)
    val NAMES_FOR_PIPES: Option = Option(
      "angular.show.names.for.pipes", Angular2Bundle.messagePointer("angular.inlay.params.option.pipe.arguments"), true)
  }

  private fun isSettingsPreview(element: JSCallExpression): Boolean {
    // fast path for normal case
    var parent = element.parent
    if (parent !is Angular2Interpolation) return false
    parent = parent.getParent()
    if (parent !is ASTWrapperPsiElement) return false
    parent = parent.getParent()
    if (parent !is HtmlTag) return false
    parent = parent.getParent()
    if (parent !is HtmlTag) return false
    parent = parent.getParent()
    if (parent !is HtmlDocumentImpl) return false
    parent = parent.getParent()
    return if (parent.isPhysical || parent !is HtmlFileImpl) false else "dummy" == parent.name
  }
}
