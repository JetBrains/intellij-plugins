// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.webSymbols.PolySymbolApiStatus
import com.intellij.webSymbols.PolySymbolQualifiedKind
import org.angular2.web.Angular2Symbol
import org.angular2.web.NG_DIRECTIVE_ATTRIBUTES

interface Angular2DirectiveAttribute : Angular2Symbol, Angular2Element {

  override val name: String

  override val type: JSType?

  override val project: Project
    get() = sourceElement.project

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = NG_DIRECTIVE_ATTRIBUTES

  override val apiStatus: PolySymbolApiStatus

  override fun createPointer(): Pointer<out Angular2DirectiveAttribute>
}
