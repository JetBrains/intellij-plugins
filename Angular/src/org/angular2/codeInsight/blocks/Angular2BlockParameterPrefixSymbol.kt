// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.util.containers.Stack
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.webTypes.WebTypesSymbolBase
import com.intellij.webSymbols.webTypes.WebTypesSymbolFactory
import org.angular2.web.NG_BLOCK_PARAMETERS

class Angular2BlockParameterPrefixSymbol : WebTypesSymbolBase() {

  val parameters: List<Angular2BlockParameterSymbol>
    get() = getSymbols(NG_BLOCK_PARAMETERS, WebSymbolsListSymbolsQueryParams.create(queryExecutor, true), Stack(this))
      .filterIsInstance<Angular2BlockParameterSymbol>()

  class Factory : WebTypesSymbolFactory {
    override fun create(): WebTypesSymbolBase =
      Angular2BlockParameterPrefixSymbol()

  }
}