// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.javascript.webSymbols.apiStatus
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.model.Pointer
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.webSymbols.WebSymbolApiStatus
import org.angular2.entities.Angular2DirectiveProperty
import org.angular2.entities.Angular2EntityUtils
import java.util.*

class Angular2SourceDirectiveVirtualProperty(override val owner: TypeScriptClass,
                                             override val name: String,
                                             override val kind: String,
                                             override val required: Boolean) : Angular2DirectiveProperty {

  override val rawJsType: JSType?
    get() = null

  override val virtualProperty: Boolean
    get() = true

  override val sourceElement: JSElement
    get() = owner

  override val apiStatus: WebSymbolApiStatus
    get() = owner.apiStatus

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
    val owner = owner.createSmartPointer()
    val required = this.required
    return Pointer {
      owner.element?.let { Angular2SourceDirectiveVirtualProperty(it, name, kind, required) }
    }
  }
}
