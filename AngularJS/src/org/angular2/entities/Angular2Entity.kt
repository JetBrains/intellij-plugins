// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import org.angular2.lang.Angular2Bundle

interface Angular2Entity : Angular2Element {

  // Cannot be a property because of clash with PsiElement#getName()
  fun getName(): @NlsSafe String

  val className: String
    get() = typeScriptClass?.name ?: Angular2Bundle.message("angular.description.unnamed")

  val decorator: ES6Decorator?

  val typeScriptClass: TypeScriptClass?

  override fun createPointer(): Pointer<out Angular2Entity>
}
