// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.ivy

import com.intellij.javascript.webSymbols.apiStatus
import com.intellij.lang.javascript.psi.JSElementBase
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.JSTypeDeclaration
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.webSymbols.WebSymbolApiStatus
import org.angular2.entities.Angular2DirectiveAttribute
import org.angular2.entities.Angular2EntityUtils
import java.util.*

class Angular2IvyDirectiveAttribute internal constructor(override val name: String,
                                                         private val mySource: JSTypeDeclaration) : Angular2DirectiveAttribute {

  override val type: JSType
    get() = mySource.jsType

  override val sourceElement: PsiElement
    get() = mySource

  override val apiStatus: WebSymbolApiStatus
    get() = (mySource as? JSElementBase)?.apiStatus ?: WebSymbolApiStatus.Stable

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
    return Objects.hash(name, mySource)
  }

  override fun createPointer(): Pointer<Angular2IvyDirectiveAttribute> {
    val name = this.name
    val source = mySource.createSmartPointer()
    return Pointer {
      source.element?.let { Angular2IvyDirectiveAttribute(name, it) }
    }
  }
}
