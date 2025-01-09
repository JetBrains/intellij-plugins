// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.webSymbols.WebSymbolApiStatus
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.utils.WebSymbolDeclaredInPsi
import java.util.*

class Angular2AliasedDirectiveProperty(
  directive: Angular2Directive,
  delegate: Angular2DirectiveProperty,
  override val name: String,
  override val sourceElement: PsiElement,
  override val textRangeInSourceElement: TextRange?
) : Angular2DirectiveProperty, WebSymbolDeclaredInPsi {

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

  override val fieldName: String?
    get() = delegate.fieldName

  override val required: Boolean
    get() = delegate.required

  override val type: JSType?
    get() = delegate.type

  override val rawJsType: JSType?
    get() = delegate.rawJsType

  override val virtualProperty: Boolean
    get() = delegate.virtualProperty

  override val isSignalProperty: Boolean
    get() = delegate.isSignalProperty

  override val apiStatus: WebSymbolApiStatus
    get() = delegate.apiStatus

  override val qualifiedKind: WebSymbolQualifiedKind
    get() = delegate.qualifiedKind

  val originalName: String
    get() = delegate.name

  override fun createPointer(): Pointer<out Angular2AliasedDirectiveProperty> {
    val delegatePtr = delegate.createPointer()
    val directivePtr = directive.createPointer()
    val sourceElementPtr = sourceElement.createSmartPointer()
    val textRangeInSourceElement = textRangeInSourceElement
    val name = name
    return Pointer {
      val delegate = delegatePtr.dereference() ?: return@Pointer null
      val directive = directivePtr.dereference() ?: return@Pointer null
      val sourceElement = sourceElementPtr.dereference() ?: return@Pointer null
      Angular2AliasedDirectiveProperty(directive, delegate, name, sourceElement, textRangeInSourceElement)
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this || other is Angular2AliasedDirectiveProperty
    && other.sourceElement == sourceElement
    && other.textRangeInSourceElement == textRangeInSourceElement
    && other.delegate == delegate

  override fun hashCode(): Int =
    Objects.hash(sourceElement, textRangeInSourceElement, delegate)

  override fun toString(): String =
    Angular2EntityUtils.toString(this)

}