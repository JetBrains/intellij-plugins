// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web

import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.documentation.PolySymbolWithDocumentation

interface Angular2Symbol : PolySymbolWithDocumentation {

  val project: Project

  override val origin: PolySymbolOrigin
    get() = Angular2SymbolOrigin(this)

  override fun createPointer(): Pointer<out Angular2Symbol>

}
