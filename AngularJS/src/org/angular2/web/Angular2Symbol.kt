// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web

import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolOrigin

interface Angular2Symbol : WebSymbol {

  override val matchedName: String
    get() = name

  override val name: String

  val project: Project

  override val origin: WebSymbolOrigin
    get() = Angular2SymbolOrigin(this)

  override fun createPointer(): Pointer<out Angular2Symbol>

}
