// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolNamespace
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.query.PolySymbolsScope

abstract class VuePolySymbolBase : PolySymbolsScope {

  val namespace: PolySymbolNamespace
    get() = qualifiedKind.namespace

  val kind: PolySymbolKind
    get() = qualifiedKind.kind

  abstract val qualifiedKind: PolySymbolQualifiedKind

}