// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.webSymbols.WebSymbolsContainer

abstract class VueWebSymbolBase : WebSymbolsContainer {

  open val namespace: WebSymbolsContainer.Namespace
    get() = WebSymbolsContainer.Namespace.HTML

}