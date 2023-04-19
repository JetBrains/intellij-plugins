// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.javascript.webSymbols.apiStatus
import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.webSymbols.WebSymbol
import org.angular2.entities.Angular2DirectiveAttribute
import org.angular2.entities.Angular2EntityUtils
import java.util.*

class Angular2SourceDirectiveAttribute internal constructor(private val myParameter: JSParameter,
                                                            override val name: String) : Angular2DirectiveAttribute {

  override val type: JSType?
    get() = myParameter.jsType

  override val sourceElement: PsiElement
    get() = myParameter

  override val navigableElement: PsiElement
    get() = myParameter.navigationElement

  override val apiStatus: WebSymbol.ApiStatus?
    get() = myParameter.apiStatus

  override fun toString(): String {
    return Angular2EntityUtils.toString(this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val attribute = other as Angular2SourceDirectiveAttribute?
    return myParameter == attribute!!.myParameter && name == attribute.name
  }

  override fun hashCode(): Int {
    return Objects.hash(myParameter, name)
  }

  override fun createPointer(): Pointer<Angular2SourceDirectiveAttribute> {
    val name = this.name
    val parameter = myParameter.createSmartPointer()
    return Pointer {
      parameter.element?.let { Angular2SourceDirectiveAttribute(it, name) }
    }
  }
}
