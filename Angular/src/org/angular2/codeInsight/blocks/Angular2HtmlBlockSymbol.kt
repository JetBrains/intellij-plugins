// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.util.containers.Stack
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.JS_SYMBOLS
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.webTypes.WebTypesSymbolBase
import com.intellij.webSymbols.webTypes.WebTypesSymbolFactory
import org.angular2.web.NG_BLOCK_PARAMETERS
import org.angular2.web.NG_BLOCK_PARAMETER_PREFIXES

class Angular2HtmlBlockSymbol : WebTypesSymbolBase() {

  val isPrimary: Boolean
    get() = properties["is-primary"] == true

  val primaryBlock: String?
    get() = properties["primary-block"] as? String

  val isUnique: Boolean
    get() = properties["unique"] as? Boolean == true

  val last: Boolean
    get() = properties["order"] == "last"

  val preferredLast: Boolean
    get() = properties["order"] == "preferred-last"

  val hasNestedSecondaryBlocks: Boolean
    get() = properties["nested-secondary-blocks"] == true

  val parameters: List<Angular2BlockParameterSymbol>
    get() = getSymbols(NG_BLOCK_PARAMETERS, WebSymbolsListSymbolsQueryParams.create(queryExecutor, true), Stack(this))
      .filterIsInstance<Angular2BlockParameterSymbol>()

  val parameterPrefixes: List<Angular2BlockParameterPrefixSymbol>
    get() = getSymbols(NG_BLOCK_PARAMETER_PREFIXES, WebSymbolsListSymbolsQueryParams.create(queryExecutor, true), Stack(this))
      .filterIsInstance<Angular2BlockParameterPrefixSymbol>()

  val implicitVariables: List<WebSymbol>
    get() = getSymbols(JS_SYMBOLS, WebSymbolsListSymbolsQueryParams.create(queryExecutor, true), Stack(this))
      .filterIsInstance<WebSymbol>()

  class Factory : WebTypesSymbolFactory {
    override fun create(): WebTypesSymbolBase =
      Angular2HtmlBlockSymbol()

  }

}