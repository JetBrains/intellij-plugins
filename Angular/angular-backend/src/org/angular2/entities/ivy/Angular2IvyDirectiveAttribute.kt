// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.ivy

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.psi.JSElementBase
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.JSTypeDeclaration
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbolApiStatus
import com.intellij.polySymbols.js.apiStatus
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import org.angular2.entities.Angular2DirectiveAttribute
import org.angular2.entities.Angular2EntityUtils

class Angular2IvyDirectiveAttribute internal constructor(
  override val name: String,
  private val mySource: JSTypeDeclaration,
) : Angular2DirectiveAttribute {

  override val type: JSType
    get() = withTypeEvaluationLocation(mySource) { mySource.jsType }

  override val sourceElement: PsiElement
    get() = mySource

  override val apiStatus: PolySymbolApiStatus
    get() = (mySource as? JSElementBase)?.apiStatus ?: PolySymbolApiStatus.Stable

  override fun toString(): String {
    return Angular2EntityUtils.toString(this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val attribute = other as Angular2IvyDirectiveAttribute?
    return name == attribute!!.name && mySource == attribute.mySource
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + mySource.hashCode()
    return result
  }

  override fun createPointer(): Pointer<Angular2IvyDirectiveAttribute> {
    val name = this.name
    val source = mySource.createSmartPointer()
    return Pointer {
      source.element?.let { Angular2IvyDirectiveAttribute(name, it) }
    }
  }
}
