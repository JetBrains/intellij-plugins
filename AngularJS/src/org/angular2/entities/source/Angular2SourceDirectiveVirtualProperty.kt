// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.model.Pointer
import com.intellij.refactoring.suggested.createSmartPointer
import org.angular2.entities.Angular2DirectiveProperty
import org.angular2.entities.Angular2EntityUtils
import java.util.*

class Angular2SourceDirectiveVirtualProperty(private val myOwner: TypeScriptClass,
                                             override val name: String,
                                             override val kind: String) : Angular2DirectiveProperty {

  override val rawJsType: JSType?
    get() = null

  override val virtual: Boolean
    get() = true

  override val sourceElement: JSElement
    get() = myOwner

  override fun toString(): String {
    return Angular2EntityUtils.toString(this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val property = other as Angular2SourceDirectiveVirtualProperty?
    return myOwner == property!!.myOwner && name == property.name && kind == property.kind
  }

  override fun hashCode(): Int {
    return Objects.hash(myOwner, name, kind)
  }

  override fun createPointer(): Pointer<Angular2SourceDirectiveVirtualProperty> {
    val name = this.name
    val kind = this.kind
    val owner = myOwner.createSmartPointer()
    return Pointer {
      owner.element?.let { Angular2SourceDirectiveVirtualProperty(it, name, kind) }
    }
  }
}
