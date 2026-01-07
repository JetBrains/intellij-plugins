// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.framework.FrameworkId
import com.intellij.polySymbols.html.HtmlFrameworkSymbol
import org.angular2.Angular2Framework

interface Angular2Symbol : HtmlFrameworkSymbol {

  override val framework: FrameworkId
    get() = Angular2Framework.ID

  override val origin: PolySymbolOrigin
    get() = Angular2SymbolOrigin(this)

  override fun createPointer(): Pointer<out Angular2Symbol>

}
