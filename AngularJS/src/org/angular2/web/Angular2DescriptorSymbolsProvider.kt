// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.javascript.web.symbols.WebSymbol
import com.intellij.javascript.web.symbols.unwrapMatchedSymbols
import com.intellij.util.SmartList
import org.angular2.entities.Angular2Directive
import org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.Companion.PROP_BINDING_PATTERN
import org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.Companion.PROP_ERROR_SYMBOL
import org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.Companion.PROP_SYMBOL_DIRECTIVE

// TODO - This is a bridge between old and new API - when time comes, this should be removed.
class Angular2DescriptorSymbolsProvider(symbol: WebSymbol) {

  val nonDirectiveSymbols: List<WebSymbol>
  val errorSymbols: List<WebSymbol>
  private val directiveSymbols: List<WebSymbol>
  val directives: List<Angular2Directive>

  init {
    val nonDirectiveSymbols = SmartList<WebSymbol>()
    val errorSymbols = SmartList<WebSymbol>()
    val directiveSymbols = SmartList<WebSymbol>()
    val directives = mutableSetOf<Angular2Directive>()

    symbol.unwrapMatchedSymbols()
      .filter { !it.extension }
      .forEach { s ->
        val properties = s.properties
        if (properties[PROP_BINDING_PATTERN] == true) {
          return@forEach
        }
        if (properties[PROP_ERROR_SYMBOL] == true) {
          errorSymbols.add(s)
        }
        else {
          val directive = properties[PROP_SYMBOL_DIRECTIVE] as? Angular2Directive
          if (directive != null) {
            directiveSymbols.add(s)
            directives.add(directive)
          }
          else {
            nonDirectiveSymbols.add(s)
          }
        }
      }
    this.nonDirectiveSymbols = nonDirectiveSymbols
    this.errorSymbols = errorSymbols
    this.directiveSymbols = directiveSymbols
    this.directives = directives.toList()
  }

}