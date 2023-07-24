// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.javascript.webSymbols.apiStatus
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.model.Pointer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.webSymbols.WebSymbolApiStatus
import com.intellij.webSymbols.utils.WebSymbolDeclaredInPsi
import org.angular2.entities.Angular2DirectiveProperty
import org.angular2.entities.Angular2EntityUtils
import java.util.*

class Angular2SourceDirectiveVirtualProperty(
  override val owner: TypeScriptClass?,
  override val kind: String,
  override val name: String,
  override val required: Boolean,
  override val sourceElement: PsiElement,
  override val textRangeInSourceElement: TextRange?
) : Angular2DirectiveProperty, WebSymbolDeclaredInPsi {

  constructor(owner: TypeScriptClass, kind: String, info: Angular2PropertyInfo)
    : this(owner, kind, info.name, info.required, info.declaringElement ?: owner,
           when {
             info.declarationRange != null -> info.declarationRange
             info.declaringElement != null -> TextRange(1, 1 + info.name.length)
             else -> null
           }
  )


  override val rawJsType: JSType?
    get() = null

  override val virtualProperty: Boolean
    get() = true

  override val apiStatus: WebSymbolApiStatus
    get() = owner?.apiStatus ?: WebSymbolApiStatus.Stable

  override fun toString(): String {
    return Angular2EntityUtils.toString(this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val property = other as Angular2SourceDirectiveVirtualProperty?
    return owner == property!!.owner
           && name == property.name
           && kind == property.kind
           && required == property.required
  }

  override fun hashCode(): Int {
    return Objects.hash(owner, name, kind, required)
  }

  override fun createPointer(): Pointer<Angular2SourceDirectiveVirtualProperty> {
    val name = this.name
    val kind = this.kind
    val ownerPtr = owner?.createSmartPointer()
    val required = this.required
    val sourceElementPtr = sourceElement.createSmartPointer()
    val textRangeInSourceElement = textRangeInSourceElement
    return Pointer {
      val owner = ownerPtr?.let { it.dereference() ?: return@Pointer null }
      val sourceElement = sourceElementPtr.dereference() ?: return@Pointer null
      Angular2SourceDirectiveVirtualProperty(owner, kind, name, required, sourceElement, textRangeInSourceElement)
    }
  }
}
