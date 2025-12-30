// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.utils.unwrapMatchedSymbols
import com.intellij.util.SmartList
import org.angular2.entities.Angular2Directive

// TODO - This is a bridge between old and new API - when time comes, this should be removed.
class Angular2DescriptorSymbolsProvider(symbol: PolySymbol) {

  val nonDirectiveSymbols: List<PolySymbol>
  val errorSymbols: List<PolySymbol>
  private val directiveSymbols: List<PolySymbol>
  val directives: List<Angular2Directive>

  init {
    val nonDirectiveSymbols = SmartList<PolySymbol>()
    val errorSymbols = SmartList<PolySymbol>()
    val directiveSymbols = SmartList<PolySymbol>()
    val directives = mutableSetOf<Angular2Directive>()

    symbol.unwrapMatchedSymbols()
      .filter { !it.extension }
      .forEach { s ->
        if (s[PROP_BINDING_PATTERN] == true) {
          return@forEach
        }
        if (s[PROP_ERROR_SYMBOL] == true) {
          errorSymbols.add(s)
        }
        else {
          val directive = s[PROP_SYMBOL_DIRECTIVE]
          if (directive != null) {
            directiveSymbols.add(s)
            directives.add(directive)
          }
          else if (s.kind != NG_PROPERTY_BINDINGS) {
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