// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.model.Pointer
import com.intellij.navigation.NavigationTarget
import com.intellij.openapi.project.Project
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.SymbolNamespace
import com.intellij.webSymbols.WebSymbolDelegate
import com.intellij.webSymbols.WebSymbolOrigin

abstract class Angular2SymbolDelegate<T : Angular2Symbol>(delegate: T) : WebSymbolDelegate<T>(delegate), Angular2Symbol {

  abstract override fun createPointer(): Pointer<out Angular2SymbolDelegate<T>>

  abstract override val namespace: SymbolNamespace

  abstract override val kind: SymbolKind

  override val project: Project
    get() = delegate.project

  override val origin: WebSymbolOrigin
    get() = delegate.origin

  override val matchedName: String
    get() = name

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    super<WebSymbolDelegate>.getNavigationTargets(project)

}