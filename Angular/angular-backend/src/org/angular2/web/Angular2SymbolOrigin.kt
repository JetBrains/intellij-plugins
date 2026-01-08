// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web

import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.js.types.TypeScriptSymbolTypeSupport
import com.intellij.polySymbols.utils.PolySymbolTypeSupport

class Angular2SymbolOrigin : PolySymbolOrigin {

  override val typeSupport: PolySymbolTypeSupport?
    get() = TypeScriptSymbolTypeSupport()

  companion object {
    val empty: PolySymbolOrigin = PolySymbolOrigin.create(typeSupport = TypeScriptSymbolTypeSupport())
  }
}
