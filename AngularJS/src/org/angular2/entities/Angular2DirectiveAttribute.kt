// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.webSymbols.WebSymbol
import org.angular2.web.Angular2PsiSourcedSymbol

import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_ATTRIBUTES

interface Angular2DirectiveAttribute : Angular2PsiSourcedSymbol, Angular2Element {

  override val name: String

  override val type: JSType?

  override val source: PsiElement
    get() = sourceElement

  override val project: Project
    get() = sourceElement.project

  override val namespace: String
    get() = WebSymbol.NAMESPACE_JS

  override val kind: String
    get() = KIND_NG_DIRECTIVE_ATTRIBUTES

  override val apiStatus: WebSymbol.ApiStatus?

  override fun createPointer(): Pointer<out Angular2DirectiveAttribute>
}
