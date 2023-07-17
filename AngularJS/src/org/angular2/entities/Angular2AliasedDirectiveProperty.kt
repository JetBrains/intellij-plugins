// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.WebSymbolApiStatus

class Angular2AliasedDirectiveProperty(
  directive: Angular2Directive,
  delegate: Angular2DirectiveProperty,
  override val name: String
) : Angular2DirectiveProperty {

  private val delegate: Angular2DirectiveProperty =
    if (delegate is Angular2AliasedDirectiveProperty)
      delegate.delegate
    else
      delegate

  val directive: Angular2Directive =
    if (delegate is Angular2AliasedDirectiveProperty)
      delegate.directive
    else
      directive

  override val required: Boolean
    get() = delegate.required

  override val rawJsType: JSType?
    get() = delegate.rawJsType

  override val virtualProperty: Boolean
    get() = delegate.virtualProperty

  override val owner: TypeScriptClass?
    get() = delegate.owner

  override val apiStatus: WebSymbolApiStatus
    get() = delegate.apiStatus

  override val kind: SymbolKind
    get() = delegate.kind
  override val sourceElement: PsiElement
    get() = delegate.sourceElement

  override fun createPointer(): Pointer<out Angular2DirectiveProperty> {
    val delegatePtr = delegate.createPointer()
    val directivePtr = directive.createPointer()
    val name = name
    return Pointer {
      val delegate = delegatePtr.dereference() ?: return@Pointer null
      val directive = directivePtr.dereference() ?: return@Pointer null
      Angular2AliasedDirectiveProperty(directive, delegate, name)
    }
  }

  override fun toString(): String =
    Angular2EntityUtils.toString(this)

}