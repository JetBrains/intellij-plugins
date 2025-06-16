// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.js.JS_SYMBOLS
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.webTypes.WebTypesSymbolBase
import com.intellij.polySymbols.webTypes.WebTypesSymbolFactory
import com.intellij.util.containers.Stack
import org.angular2.web.NG_BLOCK_PARAMETERS
import org.angular2.web.NG_BLOCK_PARAMETER_PREFIXES

class Angular2HtmlBlockSymbol : WebTypesSymbolBase() {

  val isPrimary: Boolean
    get() = this[PROP_IS_PRIMARY] == true

  val primaryBlock: String?
    get() = this[PROP_PRIMARY_BLOCK]

  val isUnique: Boolean
    get() = this[PROP_UNIQUE] == true

  val last: Boolean
    get() = this[PROP_ORDER] == "last"

  val preferredLast: Boolean
    get() = this[PROP_ORDER] == "preferred-last"

  val hasNestedSecondaryBlocks: Boolean
    get() = this[PROP_NESTED_SECONDARY_BLOCKS] == true

  val parameters: List<Angular2BlockParameterSymbol>
    get() = getSymbols(NG_BLOCK_PARAMETERS, PolySymbolListSymbolsQueryParams.create(queryExecutor, true), Stack(this))
      .filterIsInstance<Angular2BlockParameterSymbol>()

  val parameterPrefixes: List<Angular2BlockParameterPrefixSymbol>
    get() = getSymbols(NG_BLOCK_PARAMETER_PREFIXES, PolySymbolListSymbolsQueryParams.create(queryExecutor, true), Stack(this))
      .filterIsInstance<Angular2BlockParameterPrefixSymbol>()

  val implicitVariables: List<PolySymbol>
    get() = getSymbols(JS_SYMBOLS, PolySymbolListSymbolsQueryParams.create(queryExecutor, true), Stack(this))

  class Factory : WebTypesSymbolFactory {
    override fun create(): WebTypesSymbolBase =
      Angular2HtmlBlockSymbol()

  }

}
