// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.editor

import com.intellij.codeInsight.hints.Option
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.editing.JavaScriptInlayParameterHintsProvider
import com.intellij.lang.javascript.psi.JSCallLikeExpression
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.expr.psi.VueJSFilterExpression

class VueJSInlayParameterHintsProvider : JavaScriptInlayParameterHintsProvider() {

  override fun getShowNameForAllArgsOption(): Option {
    return NAMES_FOR_ALL_ARGS
  }

  override fun getSupportedOptions(): List<Option> {
    return listOf(showNameForAllArgsOption, NAMES_FOR_FILTERS)
  }

  override fun isSuitableCallExpression(expression: JSCallLikeExpression?): Boolean {
    return super.isSuitableCallExpression(expression)
           && (NAMES_FOR_FILTERS.get() || expression !is VueJSFilterExpression)
  }

  override fun skipIndex(i: Int, expression: JSCallLikeExpression): Boolean {
    return if (expression is VueJSFilterExpression && i == 0) true
    else super.skipIndex(i, expression)
  }

  companion object {
    val NAMES_FOR_ALL_ARGS = Option(
      "vuejs.show.names.for.all.args", JavaScriptBundle.messagePointer("js.param.hints.show.names.for.all.args"), false)
    val NAMES_FOR_FILTERS = Option(
      "vuejs.show.names.for.filters", VueBundle.messagePointer("vue.param.hints.show.names.for.filters"), true)
  }
}
