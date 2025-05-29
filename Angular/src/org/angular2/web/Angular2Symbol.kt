// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web

import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.webSymbols.*

interface Angular2Symbol : PolySymbol {

  val project: Project

  val qualifiedKind: PolySymbolQualifiedKind

  override val kind: SymbolKind
    get() = qualifiedKind.kind

  override val namespace: SymbolNamespace
    get() = qualifiedKind.namespace

  override val origin: WebSymbolOrigin
    get() = Angular2SymbolOrigin(this)

  override fun createPointer(): Pointer<out Angular2Symbol>

}
