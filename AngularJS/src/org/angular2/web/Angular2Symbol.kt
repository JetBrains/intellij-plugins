// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolsContainer
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project

interface Angular2Symbol : WebSymbol {

  @JvmDefault
  override val matchedName: String
    get() = name

  @JvmDefault
  override val name: String

  val project: Project

  @JvmDefault
  override val origin: WebSymbolsContainer.Origin
    get() = Angular2SymbolOrigin(this)

  override fun createPointer(): Pointer<out Angular2Symbol>

}
