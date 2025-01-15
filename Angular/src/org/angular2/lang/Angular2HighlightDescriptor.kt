// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.highlighting.JSHighlightDescriptor
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import org.angular2.lang.expr.highlighting.Angular2HighlighterColors
import org.angular2.lang.expr.psi.impl.Angular2BlockParameterVariableImpl
import org.angular2.lang.expr.psi.impl.Angular2TemplateVariableImpl
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable
import org.angular2.signals.Angular2SignalUtils

enum class Angular2HighlightDescriptor(
  val attributesKey: TextAttributesKey,
  override val debugName: String,
) : JSHighlightDescriptor {

  SIGNAL(Angular2HighlighterColors.NG_SIGNAL, "ng-signal"),
  VARIABLE(Angular2HighlighterColors.NG_VARIABLE, "ng-variable"),
  ;

  override fun getAttributesKey(highlighter: JSHighlighter): TextAttributesKey = attributesKey

  companion object {
    fun getFor(resolve: PsiElement, place: PsiElement): Angular2HighlightDescriptor? =
      when {
        Angular2LangUtil.isAngular2Context(place) && withTypeEvaluationLocation(place) { Angular2SignalUtils.isSignal(resolve, place) } ->
          SIGNAL
        resolve is Angular2TemplateVariableImpl
        || resolve is Angular2HtmlAttrVariable
        || resolve is Angular2BlockParameterVariableImpl ->
          VARIABLE
        else ->
          null
      }
  }

}