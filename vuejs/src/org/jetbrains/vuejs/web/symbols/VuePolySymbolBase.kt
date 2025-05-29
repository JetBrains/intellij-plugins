// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.polySymbols.SymbolKind
import com.intellij.polySymbols.SymbolNamespace
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.PolySymbolsScope

abstract class VuePolySymbolBase : PolySymbolsScope {

  val namespace: SymbolNamespace
    get() = qualifiedKind.namespace

  val kind: SymbolKind
    get() = qualifiedKind.kind

  abstract val qualifiedKind: PolySymbolQualifiedKind

}