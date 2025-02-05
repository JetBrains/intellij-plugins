// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.editor

import com.intellij.codeInsight.hints.Option
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.editing.JavaScriptInlayParameterHintsProvider
import com.intellij.lang.javascript.psi.JSCallLikeExpression
import com.intellij.lang.javascript.psi.JSParameterItem
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.expr.psi.VueJSFilterExpression

/**
 * Does not work inside text interpolations, because they are injected PSI.
 */
class VueInlayParameterHintsProvider : JavaScriptInlayParameterHintsProvider() {

  override fun getShowNameForLiteralArgsOption(): Option {
    return Options.NAMES_FOR_LITERAL_ARGS
  }

  override fun getShowNameForAllArgsOption(): Option {
    return Options.NAMES_FOR_ALL_ARGS
  }

  override fun getSupportedOptions(): List<Option> {
    return listOf(showNameForLiteralArgsOption, showNameForAllArgsOption, Options.NAMES_FOR_FILTERS)
  }

  override fun shouldInlineParameterName(argument: PsiElement, parameter: JSParameterItem, callExpression: JSCallLikeExpression): Boolean =
    Options.NAMES_FOR_FILTERS.get() && callExpression is VueJSFilterExpression
        || super.shouldInlineParameterName(argument, parameter, callExpression)

  override fun skipIndex(i: Int, expression: JSCallLikeExpression): Boolean {
    return if (expression is VueJSFilterExpression && i == 0) true
    else super.skipIndex(i, expression)
  }

  private object Options {
    val NAMES_FOR_LITERAL_ARGS = Option(
      "vuejs.show.names.for.literal.args", JavaScriptBundle.messagePointer("js.param.hints.show.names.for.literal.args"), true)
    val NAMES_FOR_ALL_ARGS = Option(
      "vuejs.show.names.for.all.args", JavaScriptBundle.messagePointer("js.param.hints.show.names.for.all.args"), false)
    val NAMES_FOR_FILTERS = Option(
      "vuejs.show.names.for.filters", VueBundle.messagePointer("vue.param.hints.show.names.for.filters"), true)
  }

}
