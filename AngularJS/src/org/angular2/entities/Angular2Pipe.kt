// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.model.Pointer

interface Angular2Pipe : Angular2Declaration {

  val transformMethods: Collection<TypeScriptFunction>

  override fun createPointer(): Pointer<out Angular2Pipe>
}
