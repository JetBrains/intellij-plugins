// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.webTypes.WebTypesSymbolBase
import com.intellij.webSymbols.webTypes.WebTypesSymbolFactory
import org.angular2.web.Angular2WebSymbolsQueryConfigurator

class Angular2BlockParameterSymbol : WebTypesSymbolBase() {

  val isUnique: Boolean
    get() = properties["unique"] as? Boolean == true

  val isPrimaryExpression: Boolean
    get() = name == PRIMARY_EXPRESSION

  val triggers: List<WebSymbol>
    get() = queryExecutor.runListSymbolsQuery(Angular2WebSymbolsQueryConfigurator.NG_DEFER_ON_TRIGGERS, true, scope = listOf(this))

  companion object {
    const val PRIMARY_EXPRESSION = "\$primary-expression"
  }

  class Factory : WebTypesSymbolFactory {
    override fun create(): WebTypesSymbolBase =
      Angular2BlockParameterSymbol()

  }
}