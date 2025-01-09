// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.webTypes.WebTypesSymbolBase
import com.intellij.webSymbols.webTypes.WebTypesSymbolFactory
import org.angular2.web.NG_DEFER_ON_TRIGGERS

class Angular2BlockParameterSymbol : WebTypesSymbolBase() {

  val isUnique: Boolean
    get() = properties["unique"] as? Boolean == true

  val isPrimaryExpression: Boolean
    get() = name == PRIMARY_EXPRESSION

  val hasContent: Boolean
    get() = properties["no-content"] as? Boolean != true

  val triggers: List<WebSymbol>
    get() = queryExecutor.runListSymbolsQuery(NG_DEFER_ON_TRIGGERS, true, additionalScope = listOf(this))

  companion object {
    const val PRIMARY_EXPRESSION: String = "\$primary-expression"
  }

  class Factory : WebTypesSymbolFactory {
    override fun create(): WebTypesSymbolBase =
      Angular2BlockParameterSymbol()

  }
}