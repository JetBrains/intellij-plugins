// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.polySymbols.js.apiStatus
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.model.Pointer
import com.intellij.openapi.util.TextRange
import com.intellij.polySymbols.PolySymbolApiStatus
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.utils.PolySymbolDeclaredInPsi
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import org.angular2.entities.Angular2ClassBasedDirectiveProperty
import org.angular2.entities.Angular2EntityUtils

class Angular2SourceDirectiveVirtualProperty(
  override val owner: TypeScriptClass?,
  override val qualifiedKind: PolySymbolQualifiedKind,
  override val name: String,
  override val required: Boolean,
  override val sourceElement: PsiElement,
  override val textRangeInSourceElement: TextRange?,
) : Angular2ClassBasedDirectiveProperty, PolySymbolDeclaredInPsi {

  constructor(owner: TypeScriptClass, qualifiedKind: PolySymbolQualifiedKind, info: Angular2PropertyInfo)
    : this(owner, qualifiedKind, info.name, info.required, info.declaringElement ?: info.nameElement ?: owner,
           when {
             info.declarationRange != null -> info.declarationRange
             info.declaringElement != null -> TextRange(1, 1 + info.name.length)
             info.nameElement != null -> TextRange(1, 1 + info.name.length)
             else -> null
           }
  )

  override val fieldName: String?
    get() = null

  override val rawJsType: JSType?
    get() = null

  override val virtualProperty: Boolean
    get() = true

  override val isSignalProperty: Boolean
    get() = false

  override val apiStatus: PolySymbolApiStatus
    get() = owner?.apiStatus ?: PolySymbolApiStatus.Stable

  override fun toString(): String {
    return Angular2EntityUtils.toString(this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val property = other as Angular2SourceDirectiveVirtualProperty?
    return owner == property!!.owner
           && name == property.name
           && qualifiedKind == property.qualifiedKind
           && required == property.required
  }


  override fun hashCode(): Int {
    var result = owner.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + qualifiedKind.hashCode()
    result = 31 * result + required.hashCode()
    return result
  }

  override fun createPointer(): Pointer<Angular2SourceDirectiveVirtualProperty> {
    val name = this.name
    val qualifiedKind = this.qualifiedKind
    val ownerPtr = owner?.createSmartPointer()
    val required = this.required
    val sourceElementPtr = sourceElement.createSmartPointer()
    val textRangeInSourceElement = textRangeInSourceElement
    return Pointer {
      val owner = ownerPtr?.let { it.dereference() ?: return@Pointer null }
      val sourceElement = sourceElementPtr.dereference() ?: return@Pointer null
      Angular2SourceDirectiveVirtualProperty(owner, qualifiedKind, name, required, sourceElement, textRangeInSourceElement)
    }
  }
}
