// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement

interface VueRegularComponent : VueComponent, VueContainer {

  val typeParameters: List<TypeScriptTypeParameter>

  val nameElement: PsiElement?
  override fun createPointer(): Pointer<out VueRegularComponent>
}
