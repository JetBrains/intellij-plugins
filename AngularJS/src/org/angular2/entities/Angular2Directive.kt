// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.model.Pointer
import org.angular2.web.Angular2Symbol

interface Angular2Directive : Angular2Declaration {

  val selector: Angular2DirectiveSelector

  val exportAsList: List<String>

  val inputs: Collection<Angular2DirectiveProperty>
    get() = bindings.inputs

  val outputs: Collection<Angular2DirectiveProperty>
    get() = bindings.outputs

  val inOuts: List<Angular2Symbol>
    get() = bindings.inOuts

  val bindings: Angular2DirectiveProperties

  val attributes: Collection<Angular2DirectiveAttribute>

  val directiveKind: Angular2DirectiveKind

  val isComponent: Boolean
    get() = false

  override fun createPointer(): Pointer<out Angular2Directive>

  companion object {

    val EMPTY_ARRAY = arrayOfNulls<Angular2Directive>(0)
  }
}
