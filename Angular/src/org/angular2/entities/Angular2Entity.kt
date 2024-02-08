// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement

interface Angular2Entity : Angular2Element {

  // Cannot be a property because of clash with PsiElement#getName()
  fun getName(): @NlsSafe String

  val entitySource: PsiElement?

  val entitySourceName: String

  val entityJsType: JSType?

  val isModifiable: Boolean

  override fun createPointer(): Pointer<out Angular2Entity>
}
