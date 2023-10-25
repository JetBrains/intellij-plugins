// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.SymbolNamespace
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScope

abstract class VueWebSymbolBase : WebSymbolsScope {

  val namespace: SymbolNamespace
    get() = qualifiedKind.namespace

  val kind: SymbolKind
    get() = qualifiedKind.kind

  abstract val qualifiedKind: WebSymbolQualifiedKind

}