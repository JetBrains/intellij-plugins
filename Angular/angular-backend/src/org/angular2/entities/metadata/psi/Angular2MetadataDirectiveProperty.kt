// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import com.intellij.javascript.web.js.WebJSTypesUtil.wrapWithUndefinedIfOptional
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.psi.JSElementBase
import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.model.Pointer
import com.intellij.openapi.util.NullableLazyValue
import com.intellij.openapi.util.NullableLazyValue.lazyNullable
import com.intellij.polySymbols.PolySymbolApiStatus
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.js.apiStatus
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.coalesceApiStatus
import com.intellij.polySymbols.utils.coalesceWith
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.util.asSafely
import org.angular2.codeInsight.Angular2LibrariesHacks
import org.angular2.entities.Angular2ClassBasedDirectiveProperty
import org.angular2.entities.Angular2EntityUtils

class Angular2MetadataDirectiveProperty internal constructor(
  private val myOwner: Angular2MetadataClassBase<*>,
  private val myFieldName: String,
  override val name: String,
  override val kind: PolySymbolKind,
) : Angular2ClassBasedDirectiveProperty, PsiSourcedPolySymbol {

  private val mySignature: NullableLazyValue<JSRecordType.PropertySignature> =
    lazyNullable { myOwner.getPropertySignature(myFieldName) }

  override val rawJsType: JSType?
    get() = withTypeEvaluationLocation(myOwner.typeScriptClass) {
      mySignature.value?.let {
        Angular2LibrariesHacks.hackQueryListTypeInNgForOf(it.jsType, this)?.wrapWithUndefinedIfOptional(it)
      }
    }

  override val virtualProperty: Boolean
    get() = mySignature.value == null

  override val isSignalProperty: Boolean
    get() = false

  override val apiStatus: PolySymbolApiStatus
    get() = coalesceApiStatus(mySignature.value?.memberSource?.allSourceElements) { (it as? JSElementBase)?.apiStatus }
      .coalesceWith(myOwner.sourceElement.asSafely<JSElementBase>()?.apiStatus)

  override val source: PsiElement
    get() = sourceElement

  override val sourceElement: PsiElement
    get() = mySignature.value?.memberSource?.singleElement ?: myOwner.sourceElement

  override val required: Boolean
    get() = false

  override val owner: TypeScriptClass?
    get() = myOwner.typeScriptClass


  override val fieldName: String?
    get() = mySignature.value?.memberName

  override fun createPointer(): Pointer<Angular2MetadataDirectiveProperty> {
    val owner = myOwner.createSmartPointer()
    val name = this.name
    val fieldName = myFieldName
    val kind = this.kind
    return Pointer {
      owner.dereference()?.let { Angular2MetadataDirectiveProperty(it, fieldName, name, kind) }
    }
  }

  override fun toString(): String {
    return Angular2EntityUtils.toString(this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val property = other as Angular2MetadataDirectiveProperty?
    return myFieldName == property!!.myFieldName &&
           name == property.name &&
           kind == property.kind &&
           myOwner == property.myOwner
  }

  override fun hashCode(): Int {
    var result = myFieldName.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + kind.hashCode()
    result = 31 * result + myOwner.hashCode()
    return result
  }
}
