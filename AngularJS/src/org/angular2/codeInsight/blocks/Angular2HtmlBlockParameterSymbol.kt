// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.webSymbols.webTypes.WebTypesSymbolBase
import com.intellij.webSymbols.webTypes.WebTypesSymbolFactory

class Angular2HtmlBlockParameterSymbol : WebTypesSymbolBase() {

  val maxCount: Int?
    get() = (properties["max-count"] as? Number)?.toInt()

  val isPrimaryExpression: Boolean
    get() = name == PRIMARY_EXPRESSION

  companion object {
    const val PRIMARY_EXPRESSION = "\$primary-expression"
  }

  class Factory : WebTypesSymbolFactory {
    override fun create(): WebTypesSymbolBase =
      Angular2HtmlBlockParameterSymbol()

  }
}